package com.salesianos.dam.primerjemplo.validation;

import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class UniqueProductNameValidator
        implements ConstraintValidator<UniqueProductName, String> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.hasText(value) && !productRepository.existsByName(value);
    }
}
