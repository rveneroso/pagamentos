package br.gov.mg.tce.pagamentos.mapper;

import br.gov.mg.tce.pagamentos.dto.request.UsuarioRequestDTO;
import br.gov.mg.tce.pagamentos.dto.response.UsuarioResponseDTO;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    @Test
    void shouldConvertUsuarioRequestDTOToUsuario() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNomeCompleto("João Silva");
        dto.setNumeroDocumento("52998224725");
        dto.setEmail("joao@email.com");
        dto.setSenha("123456");
        dto.setTipo(TipoUsuario.PF);

        Usuario usuario = UsuarioMapper.toEntity(dto);

        assertNotNull(usuario);
        assertEquals(dto.getNomeCompleto(), usuario.getNomeCompleto());
        assertEquals(dto.getNumeroDocumento(), usuario.getNumeroDocumento());
        assertEquals(dto.getEmail(), usuario.getEmail());
        assertEquals(dto.getSenha(), usuario.getSenha());
        assertEquals(dto.getTipo(), usuario.getTipo());
    }

    @Test
    void shouldConvertUsuarioToUsuarioResponseDTO() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("Maria Souza");
        usuario.setNumeroDocumento("12345678901");
        usuario.setEmail("maria@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setTipo(TipoUsuario.PF);

        UsuarioResponseDTO dto = UsuarioMapper.toResponse(usuario);

        assertNotNull(dto);
        assertEquals(usuario.getId(), dto.id());
        assertEquals(usuario.getNomeCompleto(), dto.nomeCompleto());
        assertEquals(usuario.getNumeroDocumento(), dto.numeroDocumento());
        assertEquals(usuario.getEmail(), dto.email());
        assertEquals(usuario.getTipo(), dto.tipo());
    }

    @Test
    void shouldNotReturnPasswordInUsuarioResponseDTO() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("Carlos");
        usuario.setNumeroDocumento("12345678901");
        usuario.setEmail("carlos@email.com");
        usuario.setSenha("senha-secreta");
        usuario.setTipo(TipoUsuario.PF);

        UsuarioResponseDTO dto = UsuarioMapper.toResponse(usuario);

        assertNotNull(dto);

        // Garante que o DTO de resposta não possui atributo senha
        assertDoesNotThrow(() -> UsuarioResponseDTO.class.getDeclaredField("id"));
        assertThrows(NoSuchFieldException.class,
                () -> UsuarioResponseDTO.class.getDeclaredField("senha"));
    }
}