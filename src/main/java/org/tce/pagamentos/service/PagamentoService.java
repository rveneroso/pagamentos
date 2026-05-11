package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.tce.pagamentos.dto.request.PagamentoRequestDTO;
import org.tce.pagamentos.entity.*;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.enums.TipoUsuario;
import org.tce.pagamentos.exception.BusinessException;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        validarRegrasIniciais(pagador, dto.getValor());

        Pagamento pagamento = new Pagamento();
        pagamento.setPagador(pagador);
        pagamento.setRecebedor(recebedor);
        pagamento.setValor(dto.getValor());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataCriacao(LocalDateTime.now());

        Pagamento salvo = pagamentoRepository.save(pagamento);


        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // Se houver uma transação ativa, só dispara o assíncrono APÓS o sucesso do commit da transação atual
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pagamentoProcessor.processarPagamentoAsync(salvo.getId());
                }
            });
        } else {
            // Fallback caso alguém remova o @Transactional por erro no futuro
            pagamentoProcessor.processarPagamentoAsync(salvo.getId());
        }

        return salvo;
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

    private void validarRegrasIniciais(Usuario pagador, BigDecimal valor) {

        if(valor.doubleValue() <= 0) {
            throw new BusinessException("O valor a ser pago deve ser maior que zero");
        }

        if (pagador.getTipo() == TipoUsuario.PJ) {
            throw new BusinessException("Lojistas não podem realizar pagamentos");
        }

        if (pagador.getSaldo().compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para essa transação");
        }
    }
}