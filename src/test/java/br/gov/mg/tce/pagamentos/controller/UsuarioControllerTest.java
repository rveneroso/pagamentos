package br.gov.mg.tce.pagamentos.controller;

import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "security.api-key=secret-key-usuarios")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private final String API_KEY_HEADER = "x-api-key";
    private final String VALID_KEY = "secret-key-usuarios";

    @Test
    void shouldSaveUsuarioWithSuccess() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNomeCompleto("João Silva");
        request.setNumeroDocumento("39053344705");
        request.setSenha("yJ*&!620BW");
        request.setTipo(TipoUsuario.PF);
        request.setEmail("joao@tce.org");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNomeCompleto("João Silva");
        usuarioSalvo.setNumeroDocumento("39053344705");
        usuarioSalvo.setTipo(TipoUsuario.PF);
        usuarioSalvo.setEmail("joao@tce.org");

        when(usuarioService.salvar(any(UsuarioRequestDTO.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(post("/usuarios")
                        .header(API_KEY_HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.numeroDocumento").value("39053344705"))
                .andExpect(jsonPath("$.tipo").value("PF"))
                .andExpect(jsonPath("$.email").value("joao@tce.org"));
    }

    @Test
    void shouldListAllUsuarios() throws Exception {
        Usuario u1 = new Usuario(); u1.setId(1L); u1.setNomeCompleto("User One");
        Usuario u2 = new Usuario(); u2.setId(2L); u2.setNomeCompleto("User Two");

        when(usuarioService.listarTodos()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/usuarios")
                        .header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nomeCompleto").value("User One"))
                .andExpect(jsonPath("$[1].nomeCompleto").value("User Two"));
    }

    @Test
    void shouldBlockRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .header(API_KEY_HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}