package br.gov.mg.tce.pagamentos.dto.response;

import lombok.Data;
import br.gov.mg.tce.pagamentos.enums.TipoUsuario;
import br.gov.mg.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

@Data
@ValidaDocumentoPorTipo
public class UsuarioResponseDTO {

    private Long id;

    private String nomeCompleto;

    private String numeroDocumento;

    private String email;

    private TipoUsuario tipo;
}