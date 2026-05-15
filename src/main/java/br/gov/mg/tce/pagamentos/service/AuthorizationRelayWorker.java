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
public class AuthorizationRelayWorker {

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoProcessor pagamentoProcessor;

    @Scheduled(fixedDelay = 60000)
    public void processarPendentes() {
        log.info("AuthorizationRelayWorker.processarPendentes -  ciclo de retentativa iniciado.");
        List<Pagamento> falhasParaRetentativa = pagamentoRepository
                .findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO);

        if (falhasParaRetentativa.isEmpty()) {
            log.info("AuthorizationRelayWorker.processarPendentes -  não há pagamentos pendentes.");
            return;
        }

        log.info("AuthorizationRelayWorker.processarPendentes - Iniciando retentativa para {} pagamentos", falhasParaRetentativa.size());

        for (Pagamento pagamento : falhasParaRetentativa) {
            try {
                // Chamada SÍNCRONA: aproveita a thread do próprio Worker
                // e processa um por um, evitando picos de CPU/Threads
                pagamentoProcessor.autorizar(pagamento.getId());

            } catch (Exception e) {
                log.error("Erro na retentativa do pagamento {}: {}", pagamento.getId(), e.getMessage());
            }
        }
    }
}