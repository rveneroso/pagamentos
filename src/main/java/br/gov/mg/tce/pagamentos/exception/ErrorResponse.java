package br.gov.mg.tce.pagamentos.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Estrutura padrão para retorno de erros da API")
public record ErrorResponse(

        @Schema(description = "Código interno do erro", example = "BUSINESS_ERROR")
        String code,

        @Schema(description = "Mensagem detalhada sobre o erro", example = "Saldo insuficiente")
        String message,

        @Schema(description = "Data e hora em que o erro ocorreu")
        LocalDateTime timestamp
) {}