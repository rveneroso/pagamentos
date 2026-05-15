package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.AuthorizationClient;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.exception.BusinessException;
import br.gov.mg.tce.pagamentos.repository.EmailOutboxRepository;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import br.gov.mg.tce.pagamentos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoProcessor {

    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailOutboxRepository emailOutboxRepository;
    private final AuthorizationClient authorizationClient;
    private final TransactionTemplate transactionTemplate;
    private final EmailOutboxService emailOutboxService;
    private final PagamentoAuthorizer pagamentoAuthorizer;

    @Async
    public void processarPagamentoAsync(Long pagamentoId) {
        autorizar(pagamentoId);
    }

    public void autorizar(Long pagamentoId) {
        log.info("Iniciando fase de autorização para o ID: {}", pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new BusinessException("Pagamento não encontrado"));

        if (!pagamento.podeSerAutorizado()) {
            return;
        }

        try {
            log.debug("PagamentoProcessor.autorizar - Chamando autorizador para o pagamento ID: {}", pagamentoId);
            boolean autorizado = pagamentoAuthorizer.obterAutorizacao(pagamento);
            log.debug("PagamentoProcessor.autorizar - Resposta do serviço de autorização para o pagamento ID: {} - {} ",pagamentoId, autorizado);

            if (autorizado) {
                transactionTemplate.executeWithoutResult(status -> {
                    pagamento.autorizar();
                    pagamentoRepository.save(pagamento);
                    executar(pagamentoId);
                });
            } else {
                transactionTemplate.executeWithoutResult(status -> {
                    pagamento.finalizarComErro("Não autorizado", StatusPagamento.CANCELADO);
                    pagamentoRepository.save(pagamento);
                });
            }

        } catch (Exception ex) {
            transactionTemplate.executeWithoutResult(status -> {
                pagamento.finalizarComErro("Serviço de autorização indisponível", StatusPagamento.ERRO_AUTORIZACAO);
                pagamentoRepository.save(pagamento);
            });
        }
    }

    public void executar(Long pagamentoId) {
        log.info("Iniciando execução financeira do pagamento ID: {}", pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new BusinessException("Pagamento não encontrado"));

        if (pagamento.getStatus() != StatusPagamento.AUTORIZADO) return;

        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o status para PROCESSANDO", pagamentoId);
        pagamento.marcarComoProcessando();
        pagamentoRepository.saveAndFlush(pagamento);
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o status para PROCESSANDO", pagamentoId);

        // Locks de banco (Pessimistic Write) para garantir consistência
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai obter os registros do pagador e do recebedor.", pagamentoId);
        Usuario pagador = usuarioRepository.findByIdForUpdate(pagamento.getPagador().getId())
                .orElseThrow(() -> new BusinessException("Pagador não encontrado"));

        Usuario recebedor = usuarioRepository.findByIdForUpdate(pagamento.getRecebedor().getId())
                .orElseThrow(() -> new BusinessException("Recebedor não encontrado"));
        log.debug("PagamentoProcessor.executar - Pagamento ID: {} obteve os registros do pagador e do recebedor.", pagamentoId);

        try {
            log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o saldo do pagador e do recebedor.", pagamentoId);
            pagador.debitar(pagamento.getValor());
            recebedor.creditar(pagamento.getValor());

            usuarioRepository.save(pagador);
            usuarioRepository.save(recebedor);
            log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o status para CONCLUIDO", pagamentoId);

            log.debug("PagamentoProcessor.executar - Pagamento ID: {} vai atualizar o status para CONCLUIDO", pagamentoId);
            pagamento.concluir();
            log.debug("PagamentoProcessor.executar - Pagamento ID: {} atualizou o status para CONCLUIDO", pagamentoId);

            log.info("PagamentoProcessor.executar - Vai salvar as informações de email do pagamento ID: {}",pagamentoId);
            emailOutboxService.agendarNotificacaoRecebimento(recebedor.getEmail(), pagamento.getValor());
            log.info("PagamentoProcessor.executar - Salvou as informações de email do pagamento ID: {}",pagamentoId);

        } catch (BusinessException ex) {
            pagamento.finalizarComErro(ex.getMessage(), StatusPagamento.CANCELADO);
        }

        pagamentoRepository.save(pagamento);
        log.info("Pagamento ID: {} finalizado com status {}", pagamentoId, pagamento.getStatus());
    }
}