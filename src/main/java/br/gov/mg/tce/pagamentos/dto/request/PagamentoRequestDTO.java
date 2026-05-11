package br.gov.mg.tce.pagamentos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagamentoRequestDTO {

    @NotBlank(message = "Número do documento do pagador é obrigatório")
    private String pagadorDocumento;

    @NotBlank(message = "Número do documento do recebedor é obrigatório")
    private String recebedorDocumento;

    @NotNull(message = "Valor a ser pago é obrigatório")
    private BigDecimal valor;

}