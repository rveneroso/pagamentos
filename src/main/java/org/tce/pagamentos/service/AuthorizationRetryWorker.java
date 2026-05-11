package org.tce.pagamentos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.PagamentoRepository;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AuthorizationRetryWorker {

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoWorker pagamentoWorker;

    //@Scheduled(fixedDelay = 30000)
    public void retryAutorizacao() {

        List<Pagamento> pagamentos =
                pagamentoRepository.findByStatus(StatusPagamento.ERRO_AUTORIZACAO);

        for (Pagamento pagamento : pagamentos) {
            pagamentoWorker.processar(pagamento.getId());
        }
    }
}