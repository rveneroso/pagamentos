package br.gov.mg.tce.pagamentos.validation.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    @Test
    void shouldReturnFalseWhenNull() {
        assertFalse(CpfValidator.isValid(null));
    }

    @Test
    void shouldReturnFalseWhenInvalidFormat() {
        assertFalse(CpfValidator.isValid("123"));
        assertFalse(CpfValidator.isValid("abc"));
        assertFalse(CpfValidator.isValid("1234567890a"));
        assertFalse(CpfValidator.isValid("123456789012"));
    }

    @Test
    void shouldReturnFalseForCpfWithAllDigitsTheSame() {
        assertFalse(CpfValidator.isValid("11111111111"));
        assertFalse(CpfValidator.isValid("00000000000"));
        assertFalse(CpfValidator.isValid("99999999999"));
    }

    @Test
    void shouldReturnFalseForInvalidCpf() {
        assertFalse(CpfValidator.isValid("12345678900"));
        assertFalse(CpfValidator.isValid("11144477734"));
    }

    @Test
    void shouldReturnTrueForValidCpf() {
        assertTrue(CpfValidator.isValid("52998224725"));
        assertTrue(CpfValidator.isValid("11144477735"));
    }

    @Test
    void shouldIgnoreCpfWithNonNumericCharacters() {
        assertFalse(CpfValidator.isValid("390.533.447-05"));
    }
}