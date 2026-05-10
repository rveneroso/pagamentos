package org.tce.pagamentos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagamentoRequestDTO {

    @NotBlank(message = "Número do documento do pagador é obrigatório")
    private String pagadorDocumento;

    @NotBlank(message = "Número do documento do recebedor é obrigatório")
    private String recebedorDocumento;

    @NotNull(message = "Valor a ser pago é obrigatório")
    private BigDecimal valor;

}