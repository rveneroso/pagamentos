package org.tce.pagamentos.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.tce.pagamentos.validation.validator.CpfOuCnpjValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CpfOuCnpjValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOuCnpj {

    String message() default "Documento inválido (CPF ou CNPJ)";

    // Usado para agrupar validações (grupo de criação, grupo de atualização etc.)
    Class<?>[] groups() default {};

    // Permite adicionar metadados à validação (severidade, tipo de erro, logs customizados etc.)
    Class<? extends Payload>[] payload() default {};
}
