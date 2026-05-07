package org.tce.pagamentos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.validation.annotation.CpfOuCnpj;
import org.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

@Data
@ValidaDocumentoPorTipo
public class UsuarioDTO {

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "Número do documento é obrigatório")
    @CpfOuCnpj
    private String numeroDocumento;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email informado é inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatório")
    private String senha;

    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;
}