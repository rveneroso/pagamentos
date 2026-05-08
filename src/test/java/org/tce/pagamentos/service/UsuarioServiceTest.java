package org.tce.pagamentos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private UsuarioRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new UsuarioRequestDTO();
        dto.setNomeCompleto("João Silva");
        dto.setEmail("joao@email.com");
        dto.setNumeroDocumento("52998224725");
        dto.setSenha("Abc123@1");
        dto.setTipo(TipoUsuario.PF);
    }

    @Test
    void deveSalvarUsuarioComSucesso() {

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(repository.existsByNumeroDocumento(dto.getNumeroDocumento())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senha-criptografada");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setEmail(dto.getEmail());

        when(repository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        Usuario result = service.salvar(dto);

        assertNotNull(result);
        verify(repository).save(any(Usuario.class));
        verify(passwordEncoder).encode(dto.getSenha());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {

        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.salvar(dto));

        assertEquals("Email já cadastrado", ex.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDocumentoJaExiste() {

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(repository.existsByNumeroDocumento(dto.getNumeroDocumento())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.salvar(dto));

        assertEquals("Número de documento já cadastrado", ex.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    void deveListarUsuarios() {

        when(repository.findAll()).thenReturn(List.of(new Usuario()));

        List<Usuario> result = service.listarTodos();

        assertFalse(result.isEmpty());
        verify(repository).findAll();
    }
}