package br.gov.mg.tce.pagamentos.dto.response;

import br.gov.mg.tce.pagamentos.entity.Pagamento;
import br.gov.mg.tce.pagamentos.enums.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(description = "Dados do pagador") UsuarioResponseDTO pagador,
        @Schema(description = "Dados do recebedor") UsuarioResponseDTO recebedor,
        @Schema(example = "150.00") BigDecimal valor,
        @Schema(example = "CONCLUIDO") StatusPagamento status,
        @Schema(example = "Saldo insuficiente") String mensagemErro,
        @Schema(example = "2026-05-11T17:00:00") LocalDateTime dataCriacao,
        @Schema(example = "2026-05-11T17:01:00") LocalDateTime dataProcessamento
) {
    // Construtor de conversão: transforma Entity em DTO de forma simples
    public PagamentoResponseDTO(Pagamento pagamento) {
        this(
                pagamento.getId(),
                new UsuarioResponseDTO(pagamento.getPagador()),
                new UsuarioResponseDTO(pagamento.getRecebedor()),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getMensagemErro(),
                pagamento.getDataCriacao(),
                pagamento.getDataProcessamento()
        );
    }
}