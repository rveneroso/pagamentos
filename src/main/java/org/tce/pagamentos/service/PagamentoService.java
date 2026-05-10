package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tce.pagamentos.dto.request.PagamentoRequestDTO;
import org.tce.pagamentos.entity.*;
import org.tce.pagamentos.exception.BusinessException;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final UsuarioRepository usuarioRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PaymentProcessor paymentProcessor;

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

        paymentProcessor.processarPagamentoAsync(salvo.getId());

        return salvo;
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