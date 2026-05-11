package org.tce.pagamentos.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "security.api-key=test-key-123")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDenyAnyRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/pagamentos")
                        .header("x-api-key", "test-key-123"))
                .andExpect(status().isNotFound());
        // Nota: isNotFound indica que passou pelo filtro de segurança e chegou no controller (que não existe no contexto do teste)
    }

    @Test
    void shouldDenyRequestWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/pagamentos")
                        .header("x-api-key", "chave-errada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowH2ConsoleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/h2-console/index.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Se o status for 200 (OK) ou 404 (Not Found), o filtro PERMITIU a passagem.
                    // O teste só deve falhar se for 401 (bloqueado pelo seu filtro)
                    // ou 403 (bloqueado pelo CSRF/Frames do Spring Security).
                    if (status == 401) {
                        throw new AssertionError("O ApiKeyFilter barrou o acesso com 401");
                    }
                    if (status == 403) {
                        throw new AssertionError("O Spring Security barrou o acesso com 403 (CSRF/Frames)");
                    }
                });
    }
}