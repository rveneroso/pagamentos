package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import br.gov.mg.tce.pagamentos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoProcessorTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PagamentoAuthorizer pagamentoAuthorizer;

    @Mock
    private EmailOutboxService emailOutboxService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PagamentoProcessor pagamentoProcessor;

    @BeforeEach
    void setup() {
        // Informa-se ao Mockito: "Sempre que chamar executeWithoutResult, pegue a ação (Consumer) que foi passada e execute-a imediatamente."
        doAnswer(invocation -> {
            java.util.function.Consumer<org.springframework.transaction.TransactionStatus> action = invocation.getArgument(0);
            action.accept(null); // Aqui, null é passado pois o status não é usado no código
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void shouldProcessPaymentWithSuccess() {
        Long pagamentoId = 1L;
        Usuario pagador = new Usuario(1L, "Pagador", "10357592093","pagador@tce.mg.gov.br",  "e}G9Y35:", TipoUsuario.PF, new BigDecimal("1000.00"));
        Usuario recebedor = new Usuario(2L, "Recebedor", "74978548012", "recebedor@tce.mg.gov.br", "e}G9Y35:", TipoUsuario.PF,new BigDecimal("0.00"));
        Pagamento pagamento = new Pagamento(1L, pagador, recebedor, new BigDecimal("100.00"), StatusPagamento.PENDENTE, null, LocalDateTime.now(), null);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoAuthorizer.obterAutorizacao(any())).thenReturn(true);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pagador));
        when(usuarioRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recebedor));

        // Chamada ao método que é disparado pelo @Async
        pagamentoProcessor.processarPagamentoAsync(pagamentoId);

        verify(pagamentoAuthorizer).obterAutorizacao(any());
        verify(pagamentoRepository, atLeastOnce()).save(pagamento);
        verify(emailOutboxService).agendarNotificacaoRecebimento(anyString(), any());

        assert pagamento.getStatus() == StatusPagamento.CONCLUIDO;
        assert pagador.getSaldo().compareTo(new BigDecimal("900.00")) == 0;
        assert recebedor.getSaldo().compareTo(new BigDecimal("100.00")) == 0;
    }

    @Test
    void shoulCancelPaymentWhenNotAuthorized() {
        Long pagamentoId = 1L;
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(pagamentoAuthorizer.obterAutorizacao(any())).thenReturn(false);

        pagamentoProcessor.processarPagamentoAsync(pagamentoId);

        verify(pagamentoRepository).save(pagamento);
        assert pagamento.getStatus() == StatusPagamento.CANCELADO;
    }
}