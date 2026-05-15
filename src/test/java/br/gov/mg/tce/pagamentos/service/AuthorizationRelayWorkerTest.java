package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationRelayWorkerTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoProcessor pagamentoProcessor;

    @InjectMocks
    private AuthorizationRelayWorker authorizationRelayWorker;

    @Test
    void shouldProcessAllPagamentosWithRetryStatus() {
        Pagamento p1 = new Pagamento();
        p1.setId(1L);
        Pagamento p2 = new Pagamento();
        p2.setId(2L);

        List<Pagamento> payments = List.of(p1, p2);

        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(payments);

        authorizationRelayWorker.processarPendentes();

        verify(pagamentoProcessor, times(1)).autorizar(1L);
        verify(pagamentoProcessor, times(1)).autorizar(2L);
        verify(pagamentoRepository, times(1)).findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO);
    }

    @Test
    void shouldContinueProcessingWhenOnePagamentoFails() {
        Pagamento p1 = new Pagamento();
        p1.setId(1L);
        Pagamento p2 = new Pagamento();
        p2.setId(2L);

        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(List.of(p1, p2));

        doThrow(new RuntimeException("Unexpected error"))
                .when(pagamentoProcessor).autorizar(1L);

        authorizationRelayWorker.processarPendentes();

        verify(pagamentoProcessor, times(1)).autorizar(1L);
        verify(pagamentoProcessor, times(1)).autorizar(2L); // Must still call the second one
    }

    @Test
    void shouldDoNothingWhenNoPagamentosFound() {
        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(Collections.emptyList());

        authorizationRelayWorker.processarPendentes();

        verify(pagamentoProcessor, never()).autorizar(anyLong());
    }
}