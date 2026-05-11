package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.entity.*;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.EmailOutboxRepository;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagamentoProcessor {

    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailOutboxRepository emailOutboxRepository;
    private final AuthorizationClient authorizationClient;
    private final TransactionTemplate transactionTemplate;

    @Async
    public void processarPagamentoAsync(Long pagamentoId) {
        autorizar(pagamentoId);
    }

    public void autorizar(Long pagamentoId) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) return;

        try {
            System.out.println(">>> Chamando autorizador (2 min mock): " + pagamentoId);

            boolean autorizado = authorizationClient.autorizar(
                    pagamento.getPagador().getNumeroDocumento()
            );

            System.out.println(">>> Para o pagamento " + pagamentoId + " o serviço de autorização retornou " + autorizado);

            if (!autorizado) {
                transactionTemplate.execute(status -> {
                    finalizarComErro(pagamentoId, StatusPagamento.CANCELADO, "Não autorizado");
                    return null;
                });
                return;
            }

            transactionTemplate.execute(status -> {
                atualizarStatus(pagamentoId, StatusPagamento.AUTORIZADO);
                executar(pagamentoId);
                return null;
            });

        } catch (Exception ex) {
            // Salva pagamento com status específico para retry posterior
            transactionTemplate.execute(status -> {
                finalizarComErro(pagamentoId, StatusPagamento.ERRO_AUTORIZACAO, "Serviço indisponível");
                return null;
            });
        }
    }

    //@Transactional
    public void atualizarStatus(Long id, StatusPagamento status) {
        Pagamento p = pagamentoRepository.findById(id).orElseThrow();
        p.setStatus(status);
        pagamentoRepository.save(p);
        System.out.println(">>> Para o pagamento " + id + " o status agora é  " + status.name());
    }

    //@Transactional
    public void finalizarComErro(Long id, StatusPagamento status, String mensagem) {
        Pagamento p = pagamentoRepository.findById(id).orElseThrow();
        p.setStatus(status);
        p.setMensagemErro(mensagem);
        pagamentoRepository.save(p);
    }

    // Segunda etapa: se o cliente foi autorizado, tenta realizar a transação. Lock é aplicado na tabela usuarios
    //@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void executar(Long pagamentoId) {

        System.out.println(">>> Iniciando a execução do pagamento " + pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        // Certifica que o pagamento realmente foi autorizado
        if (pagamento.getStatus() != StatusPagamento.AUTORIZADO) {
            return;
        }

        System.out.println(">>> Pagamento " + pagamentoId + " vai atualizar o status para PROCESSANDO");
        pagamento.setStatus(StatusPagamento.PROCESSANDO);
        pagamentoRepository.saveAndFlush(pagamento);
        System.out.println(">>> Pagamento " + pagamentoId + " atualizou o status para PROCESSANDO");

        // Lê os registros dos usuários pagador e recebedor aplicando lock para garantir consistência na coluna saldo
        System.out.println(">>> Pagamento " + pagamentoId + " vai obter os registros do pagador e do recebedor");
        Usuario pagador = usuarioRepository.findByIdForUpdate(
                pagamento.getPagador().getId()
        ).orElseThrow(() -> new RuntimeException("Pagador não encontrado"));

        Usuario recebedor = usuarioRepository.findByIdForUpdate(
                pagamento.getRecebedor().getId()
        ).orElseThrow(() -> new RuntimeException("Recebedor não encontrado"));
        System.out.println(">>> Pagamento " + pagamentoId + " obtever os registros do pagador e do recebedor");

        BigDecimal valor = pagamento.getValor();

        // Certifica de que o saldo do usuário pagador é suficiente para realizar a transação
        System.out.println(">>> Pagamento " + pagamentoId + " vai verificar se o saldo é suficiente");
        if (pagador.getSaldo().compareTo(valor) < 0) {
            pagamento.setStatus(StatusPagamento.CANCELADO);
            pagamento.setMensagemErro("Saldo insuficiente");
            pagamentoRepository.save(pagamento);
            return;
        }
        System.out.println(">>> Pagamento " + pagamentoId + " verificou se o saldo é suficiente");

        // Atualiza os saldos do pagador e do recebedor e atualiza o status do pagamento
        System.out.println(">>> Pagamento " + pagamentoId + " vai atualizar o saldo do pagador e do recebedor");
        pagador.setSaldo(pagador.getSaldo().subtract(valor));
        recebedor.setSaldo(recebedor.getSaldo().add(valor));
        usuarioRepository.save(pagador);
        usuarioRepository.save(recebedor);
        System.out.println(">>> Pagamento " + pagamentoId + " atualizou o saldo do pagador e do recebedor");

        System.out.println(">>> Pagamento " + pagamentoId + " vai atualizar o status para CONCLUIDO");
        pagamento.setStatus(StatusPagamento.CONCLUIDO);
        pagamento.setDataProcessamento(LocalDateTime.now());
        pagamentoRepository.save(pagamento);
        System.out.println(">>> Pagamento " + pagamentoId + " atualizou o status para CONCLUIDO");

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