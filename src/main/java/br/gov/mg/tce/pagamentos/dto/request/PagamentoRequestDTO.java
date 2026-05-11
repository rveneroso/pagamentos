package br.gov.mg.tce.pagamentos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Número do documento do usuário pagador. \n" +
            "Obrigatoriamente deve ser um CPF já que Pessoas Jurídicas não podem realizar pagamentos." ,
            example = "71460238001")
    @NotBlank(message = "Número do documento do pagador é obrigatório")
    private String pagadorDocumento;

    @Schema(description = "Número do documento do usuário recebedor. \n" +
            "Pode ser tanto CPF quanto CNPJ, mas tem que ser diferente do número do documento do usuário pagador." ,
            example = "22333444000105")
    @NotBlank(message = "Número do documento do recebedor é obrigatório")
    private String recebedorDocumento;

    @Schema(description = "Valor a ser pago ao usuário recebedor. \n" +
            "Deve ser um valor maior do que zero." ,
            example = "10.00")
    @NotNull(message = "Valor a ser pago é obrigatório")
    private BigDecimal valor;

}