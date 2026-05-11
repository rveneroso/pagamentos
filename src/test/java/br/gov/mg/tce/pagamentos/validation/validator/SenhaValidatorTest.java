package br.gov.mg.tce.pagamentos.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
    void shouldReturnTrueWhenPasswordIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueWhenPasswordIsEmpty() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void shouldReturnTrueWhenPasswordIsBlank() {
        assertTrue(validator.isValid("   ", context));
    }

    // Cenários em que o dado está consistente.

    @Test
    void shouldReturnFalseWhenPasswordIsLessThanMinimumLength() {
        assertFalse(validator.isValid("Ab1@", context)); // < 8
    }

    @Test
    void shouldReturnFalseWhenPasswordIsGreaterThanMaximumLength() {
        assertFalse(validator.isValid("Abcdef1@XYZ9Q", context)); // > 12
    }

    @Test
    void shouldReturnFalseWhenPasswordContainsOneOrMoreSpaces() {
        assertFalse(validator.isValid("Abc 123@", context));
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotContainAtLeastAnUpperCaseLetter() {
        assertFalse(validator.isValid("abc123@#", context));
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotContainAtLeastAnNumericDigit() {
        assertFalse(validator.isValid("Abcdef@#", context));
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotContainAtLeastAnSpecialCharacter() {
        assertFalse(validator.isValid("Abcdef12", context));
    }

    @Test
    void shouldReturnTrueForValidPassword() {
        assertTrue(validator.isValid("Abc123@1", context));
    }

}