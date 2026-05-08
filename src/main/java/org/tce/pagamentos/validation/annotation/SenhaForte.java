package org.tce.pagamentos.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.tce.pagamentos.validation.validator.SenhaValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SenhaValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "Senha deve ter entre 8 e 12 caracteres, não pode conter espaços, deve conter ao menos: uma letra maiúscula, um número e um caractere especial";

    // Usado para agrupar validações (grupo de criação, grupo de atualização etc.)
    Class<?>[] groups() default {};

    // Permite adicionar metadados à validação (severidade, tipo de erro, logs customizados etc.)
    Class<? extends Payload>[] payload() default {};
}
