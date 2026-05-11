package org.tce.pagamentos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.PagamentoRepository;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationRetryWorkerTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoProcessor pagamentoProcessor;

    @InjectMocks
    private AuthorizationRetryWorker authorizationRetryWorker;

    @Test
    void shouldProcessAllPagamentosWithRetryStatus() {
        // Arrange
        Pagamento p1 = new Pagamento();
        p1.setId(1L);
        Pagamento p2 = new Pagamento();
        p2.setId(2L);

        List<Pagamento> payments = List.of(p1, p2);

        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(payments);

        // Act
        authorizationRetryWorker.processarPendentes();

        // Assert
        verify(pagamentoProcessor, times(1)).autorizar(1L);
        verify(pagamentoProcessor, times(1)).autorizar(2L);
        verify(pagamentoRepository, times(1)).findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO);
    }

    @Test
    void shouldContinueProcessingWhenOnePagamentoFails() {
        // Arrange
        Pagamento p1 = new Pagamento();
        p1.setId(1L);
        Pagamento p2 = new Pagamento();
        p2.setId(2L);

        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(List.of(p1, p2));

        // Simulate an exception on the first payment
        doThrow(new RuntimeException("Unexpected error"))
                .when(pagamentoProcessor).autorizar(1L);

        // Act
        authorizationRetryWorker.processarPendentes();

        // Assert
        verify(pagamentoProcessor, times(1)).autorizar(1L);
        verify(pagamentoProcessor, times(1)).autorizar(2L); // Must still call the second one
    }

    @Test
    void shouldDoNothingWhenNoPagamentosFound() {
        // Arrange
        when(pagamentoRepository.findByStatusOrderByDataCriacaoAsc(StatusPagamento.ERRO_AUTORIZACAO))
                .thenReturn(Collections.emptyList());

        // Act
        authorizationRetryWorker.processarPendentes();

        // Assert
        verify(pagamentoProcessor, never()).autorizar(anyLong());
    }
}