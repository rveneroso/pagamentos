package org.tce.pagamentos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.tce.pagamentos.client.AuthorizationClient;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.repository.EmailOutboxRepository;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoProcessorTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmailOutboxRepository emailOutboxRepository;
    @Mock private AuthorizationClient authorizationClient;
    @Mock private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PagamentoProcessor pagamentoProcessor;

    private Pagamento pagamento;
    private Usuario pagador;
    private Usuario recebedor;

    @BeforeEach
    void setUp() {
        pagador = new Usuario();
        pagador.setId(1L);
        pagador.setNumeroDocumento("123456");
        pagador.setSaldo(new BigDecimal("100.00"));

        recebedor = new Usuario();
        recebedor.setId(2L);
        recebedor.setSaldo(new BigDecimal("50.00"));
        recebedor.setEmail("recebedor@test.com");

        pagamento = new Pagamento();
        pagamento.setId(10L);
        pagamento.setPagador(pagador);
        pagamento.setRecebedor(recebedor);
        pagamento.setValor(new BigDecimal("30.00"));
        pagamento.setStatus(StatusPagamento.PENDENTE);

        // Mocking TransactionTemplate to execute the internal callback
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    void shouldProcessPagamentoSuccessfully() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar(anyString())).thenReturn(true);
        when(usuarioRepository.findByIdForUpdate(pagador.getId())).thenReturn(Optional.of(pagador));
        when(usuarioRepository.findByIdForUpdate(recebedor.getId())).thenReturn(Optional.of(recebedor));

        pagamentoProcessor.autorizar(10L);

        assertEquals(StatusPagamento.CONCLUIDO, pagamento.getStatus());
        assertEquals(new BigDecimal("70.00"), pagador.getSaldo());
        assertEquals(new BigDecimal("80.00"), recebedor.getSaldo());
        assertNull(pagamento.getMensagemErro());

        verify(emailOutboxRepository, times(1)).save(any());
        verify(pagamentoRepository, atLeastOnce()).save(pagamento);
    }

    @Test
    void shouldCancelWhenNotAuthorized() {
        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar(anyString())).thenReturn(false);

        pagamentoProcessor.autorizar(10L);

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        assertEquals("Não autorizado", pagamento.getMensagemErro());
        verify(usuarioRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void shouldSetErrorStatusOnAuthorizationException() {
        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(authorizationClient.autorizar(anyString())).thenThrow(new RuntimeException("Timeout"));

        pagamentoProcessor.autorizar(10L);

        assertEquals(StatusPagamento.ERRO_AUTORIZACAO, pagamento.getStatus());
        assertEquals("Serviço indisponível", pagamento.getMensagemErro());
    }

    @Test
    void shouldCancelWhenBalanceIsInsufficient() {
        pagamento.setValor(new BigDecimal("200.00"));
        pagamento.setStatus(StatusPagamento.AUTORIZADO);

        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(usuarioRepository.findByIdForUpdate(pagador.getId())).thenReturn(Optional.of(pagador));
        when(usuarioRepository.findByIdForUpdate(recebedor.getId())).thenReturn(Optional.of(recebedor));

        pagamentoProcessor.executar(10L);

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        assertEquals("Saldo insuficiente", pagamento.getMensagemErro());
        assertEquals(new BigDecimal("100.00"), pagador.getSaldo());
    }

    @Test
    void shouldIgnoreWhenStatusIsInvalid() {
        pagamento.setStatus(StatusPagamento.CONCLUIDO);
        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));

        pagamentoProcessor.autorizar(10L);

        verify(authorizationClient, never()).autorizar(anyString());
    }

    @Test
    void shouldThrowExceptionWhenPagamentoNotFound() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pagamentoProcessor.autorizar(99L));
    }
}