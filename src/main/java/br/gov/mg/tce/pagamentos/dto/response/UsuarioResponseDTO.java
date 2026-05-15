package br.gov.mg.tce.pagamentos.dto.response;

import br.gov.mg.tce.pagamentos.entity.Usuario;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioResponseDTO(

    @Schema(example = "1") Long id,

    @Schema(example = "Maria Oliveira") String nomeCompleto,

    @Schema(example = "11144477735") String numeroDocumento,

    @Schema(example = "maria.oliveira@email.com") String email,

    @Schema(example = "PF") TipoUsuario tipo
) {

    // Construtor de conveniência para conversão Entity -> DTO
    public UsuarioResponseDTO(Usuario usuario) {
        this(
        usuario.getId(),
        usuario.getNomeCompleto(),
        usuario.getNumeroDocumento(),
        usuario.getEmail(),
        usuario.getTipo()
        );
    }
}