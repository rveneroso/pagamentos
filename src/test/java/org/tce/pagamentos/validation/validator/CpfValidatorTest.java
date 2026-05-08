package org.tce.pagamentos.validation.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @Test
    void deveRetornarFalseParaNull() {
        assertFalse(CpfValidator.isValid(null));
    }

    @Test
    void deveRetornarFalseParaFormatoInvalido() {
        assertFalse(CpfValidator.isValid("123"));
        assertFalse(CpfValidator.isValid("abc"));
        assertFalse(CpfValidator.isValid("1234567890a"));
        assertFalse(CpfValidator.isValid("123456789012"));
    }

    @Test
    void deveRetornarFalseParaCpfComTodosDigitosIguais() {
        assertFalse(CpfValidator.isValid("11111111111"));
        assertFalse(CpfValidator.isValid("00000000000"));
        assertFalse(CpfValidator.isValid("99999999999"));
    }

    @Test
    void deveRetornarFalseParaCpfInvalido() {
        assertFalse(CpfValidator.isValid("12345678900"));
        assertFalse(CpfValidator.isValid("11144477734"));
    }

    @Test
    void deveRetornarTrueParaCpfValido() {
        assertTrue(CpfValidator.isValid("52998224725"));
        assertTrue(CpfValidator.isValid("11144477735"));
    }
}