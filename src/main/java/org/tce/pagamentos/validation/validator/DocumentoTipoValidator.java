package org.tce.pagamentos.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tce.pagamentos.dto.request.UsuarioRequestDTO;
import org.tce.pagamentos.entity.TipoUsuario;
import org.tce.pagamentos.validation.annotation.ValidaDocumentoPorTipo;

public class DocumentoTipoValidator implements ConstraintValidator<ValidaDocumentoPorTipo, UsuarioRequestDTO> {

    @Override
    public boolean isValid(UsuarioRequestDTO dto, ConstraintValidatorContext context) {

        // Retorna true indicando que a validação não se aplicará sobre o objeto com estado inválido (null ou vazio).
        if (dto == null ||
                dto.getTipo() == null ||
                dto.getNumeroDocumento() == null ||
                dto.getNumeroDocumento().isBlank()) {
            return true;
        }

        String doc = dto.getNumeroDocumento();

        if (!doc.matches("\\d+")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Documento deve conter apenas números")
                    .addPropertyNode("numeroDocumento")
                    .addConstraintViolation();
            return false;
        }

        if (dto.getTipo() == TipoUsuario.PF) {

            if (doc.length() != 11) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Para PF o documento deve ter 11 dígitos (CPF)")
                        .addPropertyNode("numeroDocumento")
                        .addConstraintViolation();
                return false;
            }

            if (!CpfValidator.isValid(doc)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "CPF inválido")
                        .addPropertyNode("numeroDocumento")
                        .addConstraintViolation();
                return false;
            }
        }

        if (dto.getTipo() == TipoUsuario.PJ) {

            if (doc.length() != 14) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Para PJ o documento deve ter 14 dígitos (CNPJ)")
                        .addPropertyNode("numeroDocumento")
                        .addConstraintViolation();
                return false;
            }

            if (!CnpjValidator.isValid(doc)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "CNPJ inválido")
                        .addPropertyNode("numeroDocumento")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
