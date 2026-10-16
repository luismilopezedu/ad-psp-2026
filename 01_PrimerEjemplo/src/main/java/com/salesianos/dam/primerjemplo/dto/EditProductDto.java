package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EditProductDto(
        @NotBlank(message = "{editProductDto.name.notBlank}") String name,
        @PositiveOrZero(message = "{editProductDto.price.PositiveOrZero}") Double price,
        String details
) {

    public Product to() {
        return Product.builder()
                .name(name)
                .price(price)
                .details(details)
                .build();
    }

}
