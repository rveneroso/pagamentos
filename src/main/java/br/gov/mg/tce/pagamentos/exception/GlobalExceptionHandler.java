package br.gov.mg.tce.pagamentos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import br.gov.mg.tce.pagamentos.enums.ErrorCode;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Erro de validação");

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        ErrorCode.VALIDATION_ERROR.name(),
                        message,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException ex) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Requisição inválida ou corpo ausente",
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String mensagem = "Valor inválido para o parâmetro '" +
                ex.getName() + "'";

        if (ex.getRequiredType() != null &&
                ex.getRequiredType().isEnum()) {

            Object[] valores =
                    ex.getRequiredType().getEnumConstants();

            mensagem += ". Valores permitidos: " +
                    Arrays.toString(valores);
        }

        ErrorResponse error = new ErrorResponse(
                "INVALID_PARAMETER",
                mensagem,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        "BUSINESS_ERROR",
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        "ENDPOINT_NOT_FOUND",
                        "Rota não encontrada",
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        return ResponseEntity.internalServerError().body(
                new ErrorResponse(
                        ErrorCode.INTERNAL_ERROR.name(),
                        "Erro interno inesperado",
                        LocalDateTime.now()
                )
        );
    }
}
