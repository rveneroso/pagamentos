package br.gov.mg.tce.pagamentos.service;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.repository.PagamentoRepository;
import br.gov.mg.tce.pagamentos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PagamentoProcessorIntegrationTest {

    @Autowired
    private PagamentoProcessor pagamentoProcessor;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PagamentoAuthorizer pagamentoAuthorizer;

    private Long pagamentoId;

    @BeforeEach
    void setup() {
        pagamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Simula a resposta do serviço externo
        when(pagamentoAuthorizer.obterAutorizacao(any())).thenReturn(true);
    }

    @Test
    void asyncRealTest() {

        // Os objetos do tipo Usuario precisam ser salvos primeiro. Caso contrário, ao tentar salvar Pagamento, os objetos Usuario estarão com Id nulo, causando falha no teste.
        Usuario pagadorObj = new Usuario(null, "Pagador", "10357592093","pagador@tce.mg.gov.br",  "e}G9Y35:", TipoUsuario.PF, new BigDecimal("1000.00"));
        pagadorObj = usuarioRepository.saveAndFlush(pagadorObj);

        Usuario recebedorObj = new Usuario(null, "Recebedor", "74978548012", "recebedor@tce.mg.gov.br", "e}G9Y35:", TipoUsuario.PF,new BigDecimal("0.00"));
        recebedorObj = usuarioRepository.saveAndFlush(recebedorObj);

        // Cria objeto do tipo Pagamento usando os objetos que já estão no banco
        Pagamento pagamento = new Pagamento(null, pagadorObj, recebedorObj, new BigDecimal("100.00"), StatusPagamento.PENDENTE, null, LocalDateTime.now(), null);

        // Persiste o objeto pagamento.
        pagamento = pagamentoRepository.saveAndFlush(pagamento);

        final Long id = pagamento.getId();

        // Executa realmente o código do método processarPagamentoAsync. Não é mock, é execução real.
        pagamentoProcessor.processarPagamentoAsync(id);

        // Verificação da condição a ser testada.

        // Bloqueia a execução do código até que a condição testada dentro de untilAsserted
        // seja verdadeira ou se o tempo máximo definido por atMost for atingido.
        await()
                // Tempo máximo aguardado até que a condição seja verdadeira. Se esse tempo for atingido sem
                // a condição ser testada, uma ConditionTimeoutException será lançada e o teste falhará.
                .atMost(10, TimeUnit.SECONDS)
                // Intervalo em que o await executa o código que está dentro de 'untilAsserted'.
                .pollInterval(1, TimeUnit.SECONDS)
                // Ignora exceções que possam ser levantadas no bloco dentro de untilAsserted. Por exemplo: como o processamento
                // do pagamento pode estar em andamento, ao verificar se o status é CONCLUIDO, haveria um AssertionError.
                // O await vai ignorar esse erro e, 1 segundo depois, vai executar o bloco novamente.
                .ignoreExceptions()
                // Bloco que define a condição a ser testada e que serve também para estabelecer o limite de espera do await.
                // Ele é o critério de sucesso. Se a lambda rodar sem lançar erro, o await entende que o sistema atingiu o estado desejado.
                .untilAsserted(() -> {
                    // Recupera o Pagamento com o id previamente definido
                    Pagamento p = pagamentoRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("O ID " + id + " não foi encontrado no H2 pela thread do Awaitility"));

                    // Uma vez encontrado o pagamento com o id definido,
                    assertEquals(StatusPagamento.CONCLUIDO, p.getStatus(), "Falha: o status do pagamento não evoluiu para CONCLUIDO");
                });
    }

    @Test
    void asyncInsufficientBalanceTest() {
        // Cria o pagador sem saldo suficiente para a operação.
        Usuario pagador = new Usuario(null, "Pagador", "10357592093", "pagador@tce.mg.gov.br", "e}G9Y35:", TipoUsuario.PF, new BigDecimal("10.00"));
        pagador = usuarioRepository.saveAndFlush(pagador);

        Usuario recebedor = new Usuario(null, "Recebedor", "74978548012", "recebedor@tce.mg.gov.br", "e}G9Y35:", TipoUsuario.PF, new BigDecimal("0.00"));
        recebedor = usuarioRepository.saveAndFlush(recebedor);

        // Cria pagamento com valor acima do saldo do pagador.
        Pagamento pagamento = new Pagamento(null, pagador, recebedor, new BigDecimal("100.00"), StatusPagamento.PENDENTE, null, LocalDateTime.now(), null);
        pagamento = pagamentoRepository.saveAndFlush(pagamento);

        final Long id = pagamento.getId();

        // Executa realmente o código do método processarPagamentoAsync. Não é mock, é execução real.
        pagamentoProcessor.processarPagamentoAsync(id);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Pagamento p = pagamentoRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("O ID " + id + " não foi encontrado"));
                    assertEquals(StatusPagamento.CANCELADO, p.getStatus(), "O pagamento deveria ter sido cancelado por saldo insuficiente");
                });
    }

    @Test
    void asyncDeniedAuthorizationTest() {
        Usuario pagadorObj = new Usuario(null, "Pagador", "10357592093","pagador@tce.mg.gov.br",  "e}G9Y35:", TipoUsuario.PF, new BigDecimal("1000.00"));
        pagadorObj = usuarioRepository.saveAndFlush(pagadorObj);

        Usuario recebedorObj = new Usuario(null, "Recebedor", "74978548012", "recebedor@tce.mg.gov.br", "e}G9Y35:", TipoUsuario.PF,new BigDecimal("0.00"));
        recebedorObj = usuarioRepository.saveAndFlush(recebedorObj);

        Pagamento pagamento = new Pagamento(null, pagadorObj, recebedorObj, new BigDecimal("100.00"), StatusPagamento.PENDENTE, null, LocalDateTime.now(), null);

        pagamento = pagamentoRepository.saveAndFlush(pagamento);

        final Long id = pagamento.getId();

        // Mock da resposta do serviço externo, forçando a não autorização do pagamento
        when(pagamentoAuthorizer.obterAutorizacao(any())).thenReturn(false);

        pagamentoProcessor.processarPagamentoAsync(id);

        await().untilAsserted(() -> {
            Pagamento p = pagamentoRepository.findById(id).get();
            assertEquals(StatusPagamento.CANCELADO, p.getStatus(), "O pagamento deveria ter sido cancelado pelo autorizador");
        });
    }


}