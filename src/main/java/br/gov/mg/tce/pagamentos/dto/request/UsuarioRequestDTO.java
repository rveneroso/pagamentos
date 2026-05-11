package br.gov.mg.tce.pagamentos.dto.request;

import br.gov.mg.tce.pagamentos.validation.annotation.SenhaForte;
import br.gov.mg.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;

@Data
@ValidaDocumentoPorTipo
public class UsuarioRequestDTO {

    @Schema(example = "Antônio Carlos Martins")
    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @Schema(description = "Número do documento identificador: \n" +
            "* **PF:** 11 dígitos (CPF) \n" +
            "* **PJ:** 14 dígitos (CNPJ) \n" +
            "Apenas caracteres numéricos.",
            example = "71460238001")
    @NotBlank(message = "Número do documento é obrigatório")
    private String numeroDocumento;

    @Schema(example = "usuario@dominio.com.br")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email informado é inválido")
    private String email;

    @Schema(description = "Deve  atender aos seguintes critérios:" +
            "ter entre 8 e 12 caracteres,\n"+
            "não pode conter espaços, \n" +
            "deve conter ao menos: uma letra maiúscula, um número e um caractere especial.",
            example = "A12@bc3d4")
    @NotBlank(message = "Senha é obrigatória")
    @SenhaForte
    private String senha;

    @Schema(description = "Valores válidos: PF e PJ" ,
            example = "PF")
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;
}