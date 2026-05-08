package org.tce.pagamentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tce.pagamentos.dto.UsuarioDTO;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.service.UsuarioService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
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

        when(service.salvar(any(UsuarioDTO.class))).thenReturn(usuario);

        UsuarioDTO dto = new UsuarioDTO();
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

        UsuarioDTO dto = new UsuarioDTO();

        dto.setNomeCompleto("");
        dto.setEmail("email-invalido");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de validação"));
    }

    @Test
    void deveListarUsuarios() throws Exception {

        when(service.listarTodos()).thenReturn(List.of(new Usuario()));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());
    }
}