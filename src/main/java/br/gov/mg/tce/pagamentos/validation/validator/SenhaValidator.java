package br.gov.mg.tce.pagamentos.validation.validator;

import br.gov.mg.tce.pagamentos.validation.annotation.SenhaForte;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidator implements ConstraintValidator<SenhaForte, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {

        // Retorna true indicando que a validação não se aplicará sobre o objeto com estado inválido (null ou vazio).
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