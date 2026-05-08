package org.tce.pagamentos.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tce.pagamentos.controller.UsuarioController;

import java.time.LocalDateTime;

// Handler só responde erros originados do contexto de usuário
@RestControllerAdvice(assignableTypes = UsuarioController.class)
public class UsuarioExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

        return ResponseEntity.status(409).body(
                new ErrorResponse(
                        ErrorCode.BUSINESS_ERROR.name(),
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {

        return ResponseEntity.status(404).body(
                new ErrorResponse(
                        ErrorCode.USER_NOT_FOUND.name(),
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }
}