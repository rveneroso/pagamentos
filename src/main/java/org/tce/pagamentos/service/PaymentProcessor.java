package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.client.NotificationClient;
import org.tce.pagamentos.entity.*;
import org.tce.pagamentos.repository.EmailOutboxRepository;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailOutboxRepository emailOutboxRepository;
    private final AuthorizationClient authorizationClient;

    @Async
    @Transactional
    public void processarPagamentoAsync(Long pagamentoId) {
        autorizar(pagamentoId);
    }

    // Primeira etapa: chama o serviço de autorização, sem aplicar lock na tabela usuarios
    @Transactional
    public void autorizar(Long pagamentoId) {

        System.out.println(">>> Iniciando a verificação de autorização do pagamento " + pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            return;
        }
        try {

            boolean autorizado = authorizationClient.autorizar(
                    pagamento.getPagador().getNumeroDocumento()
            );

            System.out.println(">>> Para o pagamento " + pagamentoId + " a autorização é " + autorizado);

            if (!autorizado) {
                pagamento.setStatus(StatusPagamento.CANCELADO);
                pagamento.setMensagemErro("Pagamento não autorizado");
                pagamentoRepository.save(pagamento);
                return;
            }

            pagamento.setStatus(StatusPagamento.AUTORIZADO);
            pagamentoRepository.save(pagamento);

            executar(pagamentoId);
        } catch (Exception ex) {

            pagamento.setStatus(StatusPagamento.ERRO_AUTORIZACAO);
            pagamento.setMensagemErro("Serviço de autorização indisponível");

            pagamentoRepository.save(pagamento);
        }
    }

    // Segunda etapa: se o cliente foi autorizado, tenta realizar a transação. Lock é aplicado na tabela usuarios
    @Transactional
    public void executar(Long pagamentoId) {

        System.out.println(">>> Iniciando a execução do pagamento " + pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        // Certifica que o pagamento realmente foi autorizado
        if (pagamento.getStatus() != StatusPagamento.AUTORIZADO) {
            return;
        }

        pagamento.setStatus(StatusPagamento.PROCESSANDO);
        pagamentoRepository.save(pagamento);

        // Lê os registros dos usuários pagador e recebedor aplicando lock para garantir consistência na coluna saldo
        Usuario pagador = usuarioRepository.findByIdForUpdate(
                pagamento.getPagador().getId()
        ).orElseThrow(() -> new RuntimeException("Pagador não encontrado"));

        Usuario recebedor = usuarioRepository.findByIdForUpdate(
                pagamento.getRecebedor().getId()
        ).orElseThrow(() -> new RuntimeException("Recebedor não encontrado"));

        BigDecimal valor = pagamento.getValor();

        // Certifica de que o saldo do usuário pagador é suficiente para realizar a transação
        if (pagador.getSaldo().compareTo(valor) < 0) {
            pagamento.setStatus(StatusPagamento.CANCELADO);
            pagamento.setMensagemErro("Saldo insuficiente");
            pagamentoRepository.save(pagamento);
            return;
        }

        // Atualiza os saldos do pagador e do recebedor e atualiza o status do pagamento
        pagador.setSaldo(pagador.getSaldo().subtract(valor));
        recebedor.setSaldo(recebedor.getSaldo().add(valor));

        pagamento.setStatus(StatusPagamento.CONCLUIDO);
        pagamento.setDataProcessamento(LocalDateTime.now());
        pagamentoRepository.save(pagamento);

        System.out.println(">>> Realizou a execução do pagamento " + pagamentoId);

        // Salva informações a serem enviadas por email
        EmailOutbox email = new EmailOutbox();
        email.setDestinatario(recebedor.getEmail());
        email.setMensagem("Pagamento recebido no valor de " + valor);
        email.setCriadoEm(LocalDateTime.now());
        emailOutboxRepository.save(email);

        System.out.println(">>> Salvou informações de email do pagamento " + pagamentoId);

    }
}