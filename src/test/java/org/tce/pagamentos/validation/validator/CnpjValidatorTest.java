package org.tce.pagamentos.validation.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CnpjValidatorTest {

    @Test
    void shouldReturnFalseWhenNull() {
        assertFalse(CnpjValidator.isValid(null));
    }

    @Test
    void shouldReturnFalseWhenInvalidFormat() {
        assertFalse(CnpjValidator.isValid("123"));
        assertFalse(CnpjValidator.isValid("abc"));
        assertFalse(CnpjValidator.isValid("1234567890123a"));
    }

    @Test
    void shouldReturnFalseForCnpjWithAllDigitsTheSame() {
        assertFalse(CnpjValidator.isValid("11111111111111"));
        assertFalse(CnpjValidator.isValid("00000000000000"));
    }

    @Test
    void shouldReturnFalseForInvalidCnpj() {
        assertFalse(CnpjValidator.isValid("12345678000100"));
    }

    @Test
    void shouldReturnTrueForValidCnpj() {
        assertTrue(CnpjValidator.isValid("11444777000161"));
    }

    @Test
    void shouldIgnoreCnpjWithNonNumericCharacters() {
        assertFalse(CnpjValidator.isValid("11.444.777/0001-61"));
    }
}