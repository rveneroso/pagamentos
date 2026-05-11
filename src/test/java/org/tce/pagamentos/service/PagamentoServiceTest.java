package org.tce.pagamentos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tce.pagamentos.dto.request.PagamentoRequestDTO;
import org.tce.pagamentos.entity.Pagamento;
import org.tce.pagamentos.entity.Usuario;
import org.tce.pagamentos.enums.StatusPagamento;
import org.tce.pagamentos.enums.TipoUsuario;
import org.tce.pagamentos.exception.BusinessException;
import org.tce.pagamentos.repository.PagamentoRepository;
import org.tce.pagamentos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private PagamentoProcessor pagamentoProcessor;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    void shouldCreatePagamentoWithSuccess() {
        Usuario pagador = createUsuario(1L, "111", TipoUsuario.PF, new BigDecimal("500.00"));
        Usuario recebedor = createUsuario(2L, "222", TipoUsuario.PJ, BigDecimal.ZERO);
        PagamentoRequestDTO dto = new PagamentoRequestDTO("111", "222", new BigDecimal("100.00"));

        when(usuarioRepository.findByNumeroDocumento("111")).thenReturn(Optional.of(pagador));
        when(usuarioRepository.findByNumeroDocumento("222")).thenReturn(Optional.of(recebedor));

        Pagamento pagamentoSalvo = new Pagamento();
        pagamentoSalvo.setId(100L);
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamentoSalvo);

        Pagamento result = pagamentoService.criarPagamento(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(pagamentoProcessor, times(1)).processarPagamentoAsync(100L);
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void shouldThrowExceptionWhenPJTriesToPay() {
        Usuario pagadorPJ = createUsuario(1L, "111", TipoUsuario.PJ, new BigDecimal("1000.00"));
        PagamentoRequestDTO dto = new PagamentoRequestDTO("111", "222", new BigDecimal("10.00"));

        when(usuarioRepository.findByNumeroDocumento("111")).thenReturn(Optional.of(pagadorPJ));
        when(usuarioRepository.findByNumeroDocumento("222")).thenReturn(Optional.of(new Usuario()));

        BusinessException ex = assertThrows(BusinessException.class, () -> pagamentoService.criarPagamento(dto));
        assertEquals("Lojistas não podem realizar pagamentos", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        Usuario pagador = createUsuario(1L, "111", TipoUsuario.PF, new BigDecimal("50.00"));
        PagamentoRequestDTO dto = new PagamentoRequestDTO("111", "222", new BigDecimal("100.00"));

        when(usuarioRepository.findByNumeroDocumento("111")).thenReturn(Optional.of(pagador));
        when(usuarioRepository.findByNumeroDocumento("222")).thenReturn(Optional.of(new Usuario()));

        assertThrows(BusinessException.class, () -> pagamentoService.criarPagamento(dto));
    }

    private Usuario createUsuario(Long id, String doc, TipoUsuario tipo, BigDecimal saldo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNumeroDocumento(doc);
        u.setTipo(tipo);
        u.setSaldo(saldo);
        return u;
    }
}