package org.tce.pagamentos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.tce.pagamentos.enums.TipoUsuario;
import org.tce.pagamentos.validation.annotation.SenhaForte;
import org.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

@Data
@ValidaDocumentoPorTipo
public class UsuarioRequestDTO {

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "Número do documento é obrigatório")
    private String numeroDocumento;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email informado é inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @SenhaForte
    private String senha;

    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;
}