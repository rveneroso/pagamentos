package org.tce.pagamentos.validation.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SenhaValidatorTest {

    private SenhaValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new SenhaValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    // Cenários em que o dado está inconsistente e a validação não se aplica.

    @Test
    void deveRetornarTrueParaSenhaNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void deveRetornarTrueParaSenhaVazia() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void deveRetornarTrueParaSenhaEmBranco() {
        assertTrue(validator.isValid("   ", context));
    }

    // Cenários em que o dado está consistente.

    @Test
    void deveRetornarFalseQuandoSenhaForMuitoCurta() {
        assertFalse(validator.isValid("Ab1@", context)); // < 8
    }

    @Test
    void deveRetornarFalseQuandoSenhaForMuitoLonga() {
        assertFalse(validator.isValid("Abcdef1@XYZ9Q", context)); // > 12
    }

    @Test
    void deveRetornarFalseQuandoSenhaContiverEspaco() {
        assertFalse(validator.isValid("Abc 123@", context));
    }

    @Test
    void deveRetornarFalseQuandoNaoTiverLetraMaiuscula() {
        assertFalse(validator.isValid("abc123@#", context));
    }

    @Test
    void deveRetornarFalseQuandoNaoTiverNumero() {
        assertFalse(validator.isValid("Abcdef@#", context));
    }

    @Test
    void deveRetornarFalseQuandoNaoTiverCaractereEspecial() {
        assertFalse(validator.isValid("Abcdef12", context));
    }

    @Test
    void deveRetornarTrueParaSenhaValida() {
        assertTrue(validator.isValid("Abc123@1", context));
    }

    @Test
    void deveRetornarTrueParaOutraSenhaValida() {
        assertTrue(validator.isValid("Senha@123", context));
    }
}