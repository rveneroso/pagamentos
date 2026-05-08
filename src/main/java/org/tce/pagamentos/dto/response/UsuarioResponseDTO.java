package org.tce.pagamentos.dto.response;

import lombok.Data;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

@Data
@ValidaDocumentoPorTipo
public class UsuarioResponseDTO {

    private Long id;

    private String nomeCompleto;

    private String numeroDocumento;

    private String email;

    private TipoUsuario tipo;
}