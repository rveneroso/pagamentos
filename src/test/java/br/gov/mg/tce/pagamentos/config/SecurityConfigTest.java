package br.gov.mg.tce.pagamentos.config;

import br.gov.mg.tce.pagamentos.service.AuthorizationRelayWorker;
import br.gov.mg.tce.pagamentos.service.EmailOutboxProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// Força o valor da propriedada a ser o mesmo que foi enviado no header
@TestPropertySource(properties = "security.api-key=secret-key-123")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationRelayWorker authorizationRelayWorker;

    @MockBean
    private EmailOutboxProcessor emailOutboxProcessor;

    @Test
    void shouldAllowH2ConsoleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Se ocorrer erro 404 está OK. Códigos 401 e 403 não são válidos.
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Console do H2 Console deveria estar acessível mas está bloqueada");
                    }
                });
    }

    @Test
    void shouldReturnUnauthorizedWithoutKey() throws Exception {
        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidApiKey() throws Exception {
        mockMvc.perform(get("/pagamentos")
                        .header("x-api-key", "secret-key-123"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Se o status for diferente de 401, a chave foi aceita pelo filtro.
                    if (status == 401) {
                        throw new AssertionError("API Key foi recusada pelo filtro");
                    }
                });
    }
}