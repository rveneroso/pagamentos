package br.gov.mg.tce.pagamentos.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
public class UsuarioResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Maria Oliveira")
    private String nomeCompleto;

    @Schema(example = "11144477735")
    private String numeroDocumento;

    @Schema(example = "maria.oliveira@email.com")
    private String email;

    @Schema(example = "PF")
    private TipoUsuario tipo;

    // Construtor de conveniência para conversão Entity -> DTO
    public UsuarioResponseDTO(Usuario usuario) {
        if (usuario != null) {
            this.id = usuario.getId();
            this.nomeCompleto = usuario.getNomeCompleto();
            this.numeroDocumento = usuario.getNumeroDocumento();
            this.email = usuario.getEmail();
            this.tipo = usuario.getTipo();
        }
    }
}