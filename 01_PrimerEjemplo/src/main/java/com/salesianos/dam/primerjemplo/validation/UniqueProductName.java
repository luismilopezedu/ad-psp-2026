package com.salesianos.dam.primerjemplo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueProductNameValidator.class)
@Documented
public @interface UniqueProductName {

    String message() default "El nombre del producto debe ser único";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
