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
    void shouldReturnTrueWhenSenhaIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueWhenSenhaIsEmpty() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void shouldReturnTrueWhenSenhaIsBlank() {
        assertTrue(validator.isValid("   ", context));
    }

    // Cenários em que o dado está consistente.

    @Test
    void shouldReturnFalseWhenSenhaIsLessThanMinimumLength() {
        assertFalse(validator.isValid("Ab1@", context)); // < 8
    }

    @Test
    void shouldReturnFalseWhenSenhaIsGreaterThanMaximumLength() {
        assertFalse(validator.isValid("Abcdef1@XYZ9Q", context)); // > 12
    }

    @Test
    void shouldReturnFalseWhenSenhaContainsOneOrMoreSpaces() {
        assertFalse(validator.isValid("Abc 123@", context));
    }

    @Test
    void shouldReturnFalseWhenSenhaDoesNotContainAtLeastAnUpperCaseLetter() {
        assertFalse(validator.isValid("abc123@#", context));
    }

    @Test
    void shouldReturnFalseWhenSenhaDoesNotContainAtLeastAnNumericDigit() {
        assertFalse(validator.isValid("Abcdef@#", context));
    }

    @Test
    void shouldReturnFalseWhenSenhaDoesNotContainAtLeastAnSpecialCharacter() {
        assertFalse(validator.isValid("Abcdef12", context));
    }

    @Test
    void shouldReturnTrueForValidSenha() {
        assertTrue(validator.isValid("Abc123@1", context));
    }

}