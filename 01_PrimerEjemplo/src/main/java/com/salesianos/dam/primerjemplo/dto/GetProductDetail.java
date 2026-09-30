package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;

public record GetProductDetail(
        Long id,
        String name,
        Double price,
        String details
) {

    public static GetProductDetail of(Product p) {
        return new GetProductDetail(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getDetails()
        );
    }

}
