package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.client.AuthorizationClient;
import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoAuthorizerTest {

    @Mock
    private AuthorizationClient authorizationClient;

    @InjectMocks
    private PagamentoAuthorizer authorizer;

    @Test
    void shouldReturnTrueWhenAuthorized() {
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("123456789");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setPagador(pagador);

        when(authorizationClient.autorizar("123456789")).thenReturn(true);

        boolean result = authorizer.obterAutorizacao(pagamento);

        assertTrue(result);
        verify(authorizationClient).autorizar("123456789");
    }

    @Test
    void shouldReturnFalseWhenDenied() {
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("987654321");

        Pagamento pagamento = new Pagamento();
        pagamento.setPagador(pagador);

        when(authorizationClient.autorizar("987654321")).thenReturn(false);

        boolean result = authorizer.obterAutorizacao(pagamento);

        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionOnTechnicalFailure() {
        Usuario pagador = new Usuario();
        pagador.setNumeroDocumento("111222333");

        Pagamento pagamento = new Pagamento();
        pagamento.setPagador(pagador);

        when(authorizationClient.autorizar(anyString()))
                .thenThrow(new RuntimeException("Service Unavailable"));

        assertThrows(RuntimeException.class, () -> authorizer.obterAutorizacao(pagamento));
    }
}