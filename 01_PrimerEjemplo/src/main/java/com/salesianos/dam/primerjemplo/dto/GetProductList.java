package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;

public record GetProductList(
        Long id,
        String name,
        Double price,
        String categoryName
) {

    public static GetProductList of(Product p) {
        return new GetProductList(p.getId(), p.getName(), p.getPrice());
    }




}

