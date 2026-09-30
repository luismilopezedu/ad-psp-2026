package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;

public record EditProductDto(
        String name,
        Double price,
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
