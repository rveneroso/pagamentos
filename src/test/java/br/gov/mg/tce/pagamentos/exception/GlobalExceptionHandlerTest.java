package br.gov.mg.tce.pagamentos.exception;

import br.gov.mg.tce.pagamentos.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessExceptionTest() {
        BusinessException ex = new BusinessException("Business rule violated");

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BUSINESS_ERROR", response.getBody().code());
        assertEquals("Business rule violated", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleValidationExceptionTest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.VALIDATION_ERROR.name(), response.getBody().code());
        assertEquals("field: must not be null", response.getBody().message());
    }

    @Test
    void handleHttpMessageNotReadableTest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Requisição inválida ou corpo ausente", response.getBody().message());
    }

    @Test
    void handleTypeMismatchExceptionEnumTest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("status");

        // Faz cast para Class para simular o retorno de um Enum
        Class<?> enumType = DummyEnum.class;
        when(ex.getRequiredType()).thenAnswer(invocation -> enumType);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Valores permitidos: [VALUE1, VALUE2]"));
    }

    @Test
    void handleNoHandlerFoundTest() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ENDPOINT_NOT_FOUND", response.getBody().code());
    }

    @Test
    void handleGenericExceptionTest() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), response.getBody().code());
        assertEquals("Erro interno inesperado", response.getBody().message());
    }

    // Helper Enum para teste
    private enum DummyEnum { VALUE1, VALUE2 }
}