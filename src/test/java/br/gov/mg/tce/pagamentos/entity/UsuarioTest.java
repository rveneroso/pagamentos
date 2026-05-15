package br.gov.mg.tce.pagamentos.entity;

import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void shouldDebitAmountCorrectly() {
        Usuario usuario = Usuario.builder()
                .saldo(new BigDecimal("500.00"))
                .build();

        usuario.debitar(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("400.00"), usuario.getSaldo());
    }

    @Test
    void shouldCreditAmountCorrectly() {
        Usuario usuario = Usuario.builder()
                .saldo(new BigDecimal("500.00"))
                .build();

        usuario.creditar(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("750.00"), usuario.getSaldo());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        Usuario usuario = Usuario.builder()
                .saldo(new BigDecimal("50.00"))
                .build();

        assertThrows(BusinessException.class, () ->
                usuario.debitar(new BigDecimal("100.00")));
    }

    @Test
    void shouldAllowPaymentForPF() {
        Usuario usuario = Usuario.builder()
                .tipo(TipoUsuario.PF)
                .build();

        assertDoesNotThrow(usuario::validarPodeRealizarPagamento);
    }

    @Test
    void shouldThrowExceptionWhenPJAttemptsToPay() {
        Usuario usuario = Usuario.builder()
                .tipo(TipoUsuario.PJ)
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                usuario::validarPodeRealizarPagamento);

        assertEquals("Lojistas não podem realizar pagamentos", exception.getMessage());
    }

    @Test
    void shouldValidateSufficientBalanceWhenEqual() {
        Usuario usuario = Usuario.builder()
                .saldo(new BigDecimal("100.00"))
                .build();

        assertDoesNotThrow(() -> usuario.validarSaldoSuficiente(new BigDecimal("100.00")));
    }
}