package org.tce.pagamentos.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.tce.pagamentos.validation.validator.DocumentoTipoValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DocumentoTipoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidaDocumentoPorTipo {

    String message() default "Documento inválido para o tipo de usuário";


    // Usado para agrupar validações (grupo de criação, grupo de atualização etc.)
    Class<?>[] groups() default {};

    // Permite adicionar metadados à validação (severidade, tipo de erro, logs customizados etc.)
    Class<? extends Payload>[] payload() default {};
}
