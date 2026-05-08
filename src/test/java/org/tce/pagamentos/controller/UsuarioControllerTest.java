package org.tce.pagamentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.service.UsuarioService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveSalvarUsuarioComSucesso() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(service.salvar(any(UsuarioRequestDTO.class))).thenReturn(usuario);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNomeCompleto("João");
        dto.setEmail("joao@email.com");
        dto.setNumeroDocumento("52998224725");
        dto.setSenha("Abc123@1");
        dto.setTipo(TipoUsuario.PF);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErroQuandoDtoForInvalido() throws Exception {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();

        dto.setNomeCompleto("Jose Maria");
        dto.setEmail("email-invalido");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deveListarUsuarios() throws Exception {

        when(service.listarTodos()).thenReturn(List.of(new Usuario()));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());
    }
}