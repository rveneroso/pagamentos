package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.AuthorizationClient;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoWorkerTest {

    @Mock
    private AuthorizationClient authorizationClient;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoProcessor pagamentoProcessor;

    @InjectMocks
    private PagamentoWorker pagamentoWorker;

    @Test
    void shouldAuthorizePagamentoWithSuccess() {
        Long pagamentoId = 1L;
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setPagador(pagador);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar("123")).thenReturn(true);

        pagamentoWorker.processar(pagamentoId);

        assertEquals(StatusPagamento.AUTORIZADO, pagamento.getStatus());
        verify(pagamentoRepository, times(1)).save(pagamento);
        verify(pagamentoProcessor, times(1)).executar(pagamentoId);
    }

    @Test
    void shouldCancelPagamentoWhenNotAuthorized() {
        Long pagamentoId = 1L;
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123");
        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setPagador(pagador);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar("123")).thenReturn(false);

        pagamentoWorker.processar(pagamentoId);

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        assertEquals("Pagamento não autorizado", pagamento.getMensagemErro());
        verify(pagamentoProcessor, never()).executar(anyLong());
    }

    @Test
    void shouldSetErrorStatusOnClientFailure() {
        Long pagamentoId = 1L;
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123");
        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setPagador(pagador);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar(anyString())).thenThrow(new RuntimeException("Timeout"));

        pagamentoWorker.processar(pagamentoId);

        assertEquals(StatusPagamento.ERRO_AUTORIZACAO, pagamento.getStatus());
        assertTrue(pagamento.getMensagemErro().contains("Falha na autorização"));
    }

    @Test
    void shouldThrowExceptionWhenPagamentoNotFound() {
        Long pagamentoId = 99L;
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pagamentoWorker.processar(pagamentoId));

        verifyNoInteractions(authorizationClient);
        verifyNoInteractions(pagamentoProcessor);
    }

    @Test
    void shouldClearErrorMessageOnSuccessfulReprocessing() {
        Long pagamentoId = 1L;
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setPagador(pagador);
        pagamento.setStatus(StatusPagamento.ERRO_AUTORIZACAO);
        pagamento.setMensagemErro("Falha anterior"); // Simula um erro que já estava no banco

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar("123")).thenReturn(true);

        pagamentoWorker.processar(pagamentoId);

        assertEquals(StatusPagamento.AUTORIZADO, pagamento.getStatus());
        assertNull(pagamento.getMensagemErro(), "A mensagem de erro deve ser limpa em caso de sucesso");
        verify(pagamentoRepository).save(pagamento);
    }
}