package org.tce.pagamentos.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tce.pagamentos.validation.annotation.CpfOuCnpj;

public class CpfOuCnpjValidator implements ConstraintValidator<CpfOuCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // Aqui irá retornar true porque a obrigatoriedade do campo já foi validada pela anotação @NotNull em UsuarioDTO
        if (value == null || value.isBlank()) {
            return true;
        }

        String doc = value.replaceAll("\\D", ""); // remove não numéricos

        if (doc.length() == 11) {
            return CpfValidator.isValid(doc);
        }

        if (doc.length() == 14) {
            return CnpjValidator.isValid(doc);
        }

        return false;
    }
}