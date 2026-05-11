package org.tce.pagamentos.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tce.pagamentos.enums.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioExceptionHandlerTest {

    private UsuarioExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UsuarioExceptionHandler();
    }

    @Test
    void handleBusinessExceptionTest() {
        String errorMessage = "Número de documento já cadastrado";
        BusinessException ex = new BusinessException(errorMessage);

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex);

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode().value());
        assertEquals(ErrorCode.BUSINESS_ERROR.name(), response.getBody().code());
        assertEquals(errorMessage, response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleNotFoundExceptionTest() {
        String errorMessage = "Usuário não encontrado com ID: 10";
        NotFoundException ex = new NotFoundException(errorMessage);

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertEquals(ErrorCode.USER_NOT_FOUND.name(), response.getBody().code());
        assertEquals(errorMessage, response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}