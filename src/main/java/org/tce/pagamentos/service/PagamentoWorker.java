package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.entity.StatusPagamento;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.entity.Pagamento;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagamentoWorker {

    private final AuthorizationClient authorizationClient;
    private final PagamentoRepository pagamentoRepository;
    private final PaymentProcessor paymentProcessor;

    @Async("taskExecutor")
    @Transactional
    public void processar(Long pagamentoId) {

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow();

        System.out.println(">>> Processando pagamento " + pagamento.getId());

        try {
            pagamento.setStatus(StatusPagamento.PROCESSANDO);

            boolean autorizado = authorizationClient.autorizar(
                    pagamento.getPagador().getNumeroDocumento()
            );

            if (!autorizado) {
                pagamento.setStatus(StatusPagamento.CANCELADO);
                pagamento.setMensagemErro("Pagamento não autorizado");
                return;
            }

            pagamento.setStatus(StatusPagamento.AUTORIZADO);
            pagamentoRepository.save(pagamento);

            paymentProcessor.executar(pagamento.getId());

        } catch (Exception ex) {
            pagamento.setStatus(StatusPagamento.ERRO_AUTORIZACAO);
            pagamento.setMensagemErro("Falha na autorização");
        }
    }
}
