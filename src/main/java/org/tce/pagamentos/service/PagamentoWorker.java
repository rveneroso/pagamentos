package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.entity.Pagamento;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoWorker {

    private final AuthorizationClient authorizationClient;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoProcessor pagamentoProcessor;

    @Async("taskExecutor")
    @Transactional
    public void processar(Long pagamentoId) {

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow();

        log.info("PagamentoWorker.processar - Iniciando processamento do pagamento ID: {}",pagamento.getId());

        try {
            pagamento.setStatus(StatusPagamento.PROCESSANDO);

            log.debug("PagamentoWorker.processar - Obtendo autorização do pagamento ID: {}",pagamento.getId());
            boolean autorizado = authorizationClient.autorizar(
                    pagamento.getPagador().getNumeroDocumento()
            );
            log.debug("PagamentoWorker.processar - Para o pagamento ID: {} a resposta é {}",pagamento.getId(), autorizado ? "Autorizado" : "Não autorizado");

            if (!autorizado) {
                pagamento.setStatus(StatusPagamento.CANCELADO);
                pagamento.setMensagemErro("Pagamento não autorizado");
                return;
            }

            log.debug("PagamentoWorker.processar - Vai atualizar o status do pagamento ID: {} para AUTORIZADO",pagamento.getId());
            pagamento.setStatus(StatusPagamento.AUTORIZADO);
            pagamento.setMensagemErro(null);
            pagamentoRepository.save(pagamento);
            log.debug("PagamentoWorker.processar - Atualizar o status do pagamento ID: {} para AUTORIZADO",pagamento.getId());

            log.debug("PagamentoWorker.processar - Vai realizar o pagamento ID: {}",pagamento.getId());
            pagamentoProcessor.executar(pagamento.getId());
            log.debug("PagamentoWorker.processar - Realizou o pagamento ID: {}",pagamento.getId());

        } catch (Exception ex) {
            pagamento.setStatus(StatusPagamento.ERRO_AUTORIZACAO);
            pagamento.setMensagemErro("Falha na autorização");
            log.warn("PagamentoWorker.processar - Falha na obtenção de autorização do pagamento ID: {}",pagamento.getId());
        }

        log.info("PagamentoWorker.processar - Finalizando processamento do pagamento ID: {}",pagamento.getId());
    }
}
