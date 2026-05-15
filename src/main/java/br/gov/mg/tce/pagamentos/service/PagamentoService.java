package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.dto.request.PagamentoRequestDTO;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.exception.BusinessException;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import br.gov.mg.tce.pagamentos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final UsuarioRepository usuarioRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoProcessor pagamentoProcessor;

    // Contexto transacional aqui é necessário para garantir que o commit do pagamento seja feito antes da execução async ter início.
    @Transactional
    public Pagamento criarPagamento(PagamentoRequestDTO dto) {
        Usuario pagador = usuarioRepository.findByNumeroDocumento(dto.getPagadorDocumento())
                .orElseThrow(() -> new BusinessException("Pagador não encontrado"));

        Usuario recebedor = usuarioRepository.findByNumeroDocumento(dto.getRecebedorDocumento())
                .orElseThrow(() -> new BusinessException("Recebedor não encontrado"));

        // A validação de regras iniciais e criação agora acontece nesse método
        Pagamento pagamento = Pagamento.novoPagamento(pagador, recebedor, dto.getValor());

        Pagamento salvo = pagamentoRepository.save(pagamento);

        dispararProcessamentoAssincrono(salvo.getId());

        return salvo;
    }

    private void dispararProcessamentoAssincrono(Long id) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pagamentoProcessor.processarPagamentoAsync(id);
                }
            });
        } else {
            pagamentoProcessor.processarPagamentoAsync(id);
        }
    }
    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    public Pagamento buscarPorId(Long id) {

        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pagamento não encontrado"));
    }

    public List<Pagamento> buscarPorStatus(StatusPagamento status) {

        return pagamentoRepository.findByStatus(status);
    }

    private void validarRegrasIniciais(Usuario pagador, Usuario recebedor, BigDecimal valor) {

        if(valor.doubleValue() <= 0) {
            throw new BusinessException("O valor a ser pago deve ser maior que zero");
        }

        if (pagador.getTipo() == TipoUsuario.PJ) {
            throw new BusinessException("Lojistas não podem realizar pagamentos");
        }

        if(pagador.getNumeroDocumento().equals(recebedor.getNumeroDocumento())) {
            throw new BusinessException("Pagador e recebedor devem ser pessoas diferentes");
        }

        if (pagador.getSaldo().compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para essa transação");
        }
    }
}