package org.tce.pagamentos.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tce.pagamentos.validation.annotation.SenhaForte;

public class SenhaValidator implements ConstraintValidator<SenhaForte, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {

        // Aqui irá retornar true porque a obrigatoriedade do campo já foi validada pela anotação @NotBlank em UsuarioDTO
        if (senha == null || senha.isBlank()) {
            return true;
        }

        if (senha.length() < 8 || senha.length() > 12) {
            return false;
        }

        if(senha.contains(" ")) {
            return false;
        }

        boolean possuiMaiuscula =
                senha.chars().anyMatch(Character::isUpperCase);

        boolean possuiNumero =
                senha.chars().anyMatch(Character::isDigit);

        boolean possuiEspecial =
                senha.matches(".*[@$!%*?&._-].*");

        return possuiMaiuscula
                && possuiNumero
                && possuiEspecial;
    }
}