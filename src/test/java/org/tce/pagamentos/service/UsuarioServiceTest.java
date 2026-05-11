package org.tce.pagamentos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.enums.TipoUsuario;
import org.tce.pagamentos.exception.BusinessException;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void shouldSaveUsuarioPFUWithSuccess() {
        UsuarioRequestDTO dto = createDto(TipoUsuario.PF);
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByNumeroDocumento(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario result = service.salvar(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getSaldo());
        assertEquals("encoded_password", result.getSenha());
        verify(repository, times(1)).save(any(Usuario.class));
    }

    @Test
    void shouldSaveUsuarioPJWithSuccess() {
        UsuarioRequestDTO dto = createDto(TipoUsuario.PJ);
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByNumeroDocumento(anyString())).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario result = service.salvar(dto);

        assertEquals(BigDecimal.ZERO, result.getSaldo());
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        UsuarioRequestDTO dto = createDto(TipoUsuario.PF);
        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.salvar(dto));
        assertEquals("Email já cadastrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDocumentExists() {
        UsuarioRequestDTO dto = createDto(TipoUsuario.PF);
        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(repository.existsByNumeroDocumento(dto.getNumeroDocumento())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.salvar(dto));
        assertEquals("Número de documento já cadastrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldListAllUsuarios() {
        when(repository.findAll()).thenReturn(List.of(new Usuario(), new Usuario()));

        List<Usuario> result = service.listarTodos();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // Helper para a criação de DTO
    private UsuarioRequestDTO createDto(TipoUsuario tipo) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNomeCompleto("Test User");
        dto.setEmail("test@tce.org");
        dto.setNumeroDocumento("123456789");
        dto.setSenha("password123");
        dto.setTipo(tipo);
        return dto;
    }
}