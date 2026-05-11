package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.entity.EmailOutbox;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.EmailOutboxRepository;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
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
        log.info("PagamentoProcessor.autorizar - Iniciando processamento do pagamento ID: {}",pagamentoId);
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        // Processa pagamentos tanto do fluxo inicial (PENDENTE) quanto da retentativa (ERRO_AUTORIZACAO)
        if (pagamento.getStatus() != StatusPagamento.PENDENTE &&
                pagamento.getStatus() != StatusPagamento.ERRO_AUTORIZACAO) {
            return;
        }

        try {
            log.debug("PagamentoProcessor.autorizar - Chamando autorizador para o pagamento ID: {}", pagamentoId);

            boolean autorizado = authorizationClient.autorizar(
                    pagamento.getPagador().getNumeroDocumento()
            );

            log.debug("PagamentoProcessor.autorizar - Resposta do serviço de autorização para o pagamento ID: {} - {} ",pagamentoId, autorizado);

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
        log.info("PagamentoProcessor.autorizar - Finalizando processamento do pagamento ID: {}",pagamentoId);
    }

    //@Transactional
    public void atualizarStatus(Long id, StatusPagamento status) {
        Pagamento p = pagamentoRepository.findById(id).orElseThrow();
        p.setStatus(status);
        pagamentoRepository.save(p);
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

        log.info("PagamentoProcessor.executar - Iniciando processamento do pagamento ID: {}",pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        // Certifica que o pagamento realmente foi autorizado
        if (pagamento.getStatus() != StatusPagamento.AUTORIZADO) {
            return;
        }

        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o status para PROCESSANDO", pagamentoId);
        pagamento.setStatus(StatusPagamento.PROCESSANDO);
        pagamentoRepository.saveAndFlush(pagamento);
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o status para PROCESSANDO", pagamentoId);

        // Lê os registros dos usuários pagador e recebedor aplicando lock para garantir consistência na coluna saldo
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai obter os registros do pagador e do recebedor.", pagamentoId);
        Usuario pagador = usuarioRepository.findByIdForUpdate(
                pagamento.getPagador().getId()
        ).orElseThrow(() -> new RuntimeException("Pagador não encontrado"));

        Usuario recebedor = usuarioRepository.findByIdForUpdate(
                pagamento.getRecebedor().getId()
        ).orElseThrow(() -> new RuntimeException("Recebedor não encontrado"));
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} obteve os registros do pagador e do recebedor.", pagamentoId);

        BigDecimal valor = pagamento.getValor();

        // Certifica de que o saldo do usuário pagador é suficiente para realizar a transação
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai verificar se o saldo é suficiente para a transação.", pagamentoId);
        if (pagador.getSaldo().compareTo(valor) < 0) {
            pagamento.setStatus(StatusPagamento.CANCELADO);
            pagamento.setMensagemErro("Saldo insuficiente");
            pagamentoRepository.save(pagamento);
            return;
        }
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} verificou se o saldo é suficiente para a transação.", pagamentoId);

        // Atualiza os saldos do pagador e do recebedor e atualiza o status do pagamento
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o saldo do pagador e do recebedor.", pagamentoId);
        pagador.setSaldo(pagador.getSaldo().subtract(valor));
        recebedor.setSaldo(recebedor.getSaldo().add(valor));
        usuarioRepository.save(pagador);
        usuarioRepository.save(recebedor);
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o saldo do pagador e do recebedor.", pagamentoId);

        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o status para CONCLUIDO", pagamentoId);
        pagamento.setStatus(StatusPagamento.CONCLUIDO);
        pagamento.setMensagemErro(null);
        pagamento.setDataProcessamento(LocalDateTime.now());
        pagamentoRepository.save(pagamento);
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o status para CONCLUIDO", pagamentoId);

        log.info("PagamentoProcessor.executar - Concluiu processamento do pagamento ID: {}",pagamentoId);

        // Salva informações a serem enviadas por email
        log.info("PagamentoProcessor.executar - Vai salvar as informações de email do pagamento ID: {}",pagamentoId);
        EmailOutbox email = new EmailOutbox();
        email.setDestinatario(recebedor.getEmail());
        email.setMensagem("Pagamento recebido no valor de " + valor);
        email.setCriadoEm(LocalDateTime.now());
        emailOutboxRepository.save(email);
        log.info("PagamentoProcessor.executar - Salvou as informações de email do pagamento ID: {}",pagamentoId);

    }
}