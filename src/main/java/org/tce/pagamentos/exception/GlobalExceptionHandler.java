package org.tce.pagamentos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler para o caso de nenhum body estar presente na requisição ou de haver inconsistências como, por exemplo, tipo de usuário diferente de PF e PJ.
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleBodyMissing(HttpMessageNotReadableException ex) {

        String message = "Requisição inválida";

        Throwable cause = ex.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormat) {

            if (invalidFormat.getTargetType().isEnum()) {

                String field = invalidFormat.getPath().get(0).getFieldName();

                Object[] values = invalidFormat.getTargetType().getEnumConstants();

                message = "Valor inválido para o campo '" + field +
                        "'. Valores permitidos: " + Arrays.toString(values);
            }

        } else if (ex.getMessage().contains("Required request body is missing")) {

            message = "O corpo da requisição é obrigatório";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Requisição inválida");
        body.put("message", message);

        return ResponseEntity.badRequest().body(body);
    }

    // Handler para erros de validação
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de validação");
        body.put("messages", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // Handler para exceções em tempo de execução
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleBusiness(RuntimeException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", ex.getMessage());

        return ResponseEntity.badRequest().body(body);
    }

    // Handler para exceções genéricas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro interno inesperado");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
