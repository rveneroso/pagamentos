package org.tce.pagamentos.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tce.pagamentos.dto.UsuarioDTO;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

public class DocumentoTipoValidator implements ConstraintValidator<ValidaDocumentoPorTipo, UsuarioDTO> {

    @Override
    public boolean isValid(UsuarioDTO dto, ConstraintValidatorContext context) {

        if (dto == null || dto.getNumeroDocumento() == null || dto.getTipo() == null) {
            return false;
        }

        String doc = dto.getNumeroDocumento();

        if (!doc.matches("\\d+")) {
            return false;
        }

        if (dto.getTipo() == TipoUsuario.PF) {
            return doc.length() == 11;
        }

        if (dto.getTipo() == TipoUsuario.PJ) {
            return doc.length() == 14;
        }

        return false;
    }
}
