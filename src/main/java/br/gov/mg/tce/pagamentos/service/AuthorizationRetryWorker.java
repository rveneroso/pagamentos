package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;

import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationRetryWorker {

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoProcessor pagamentoProcessor;

    // Executa 1 minuto após o fim da execução anterior
    @Scheduled(fixedDelay = 60000)
    public void processarPendentes() {
        log.info("AuthorizationRetryWorker.processarPendentes -  Ciclo de retentativa iniciado.");

        List<Pagamento> pagamentosParaRetry = pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO);

        if (pagamentosParaRetry.isEmpty()) {
            return;
        }

        log.info("AuthorizationRetryWorker.processarPendentes - Identificados {} pagamentos para retentativa.", pagamentosParaRetry.size());

        for (Pagamento pagamento : pagamentosParaRetry) {
            try {
                log.info("AuthorizationRetryWorker.processarPendentes - Reprocessando pagamento ID: {}", pagamento.getId());

                // Chama o método autorizar diretamente. Como ele usa TransactionTemplate internamente, cada iteração loop é uma transação nova.
                pagamentoProcessor.autorizar(pagamento.getId());

            } catch (Exception e) {
                // Loga o erro mas não interrompe o loop para não travar os outros pagamentos
                log.error("AuthorizationRetryWorker.processarPendentes - Erro inesperado ao processar pagamento ID: {} -  {}",pagamento.getId(), e.getMessage());
            }
        }

        log.info("AuthorizationRetryWorker.processarPendentes -  Ciclo de retentativa finalizado.");
    }
}