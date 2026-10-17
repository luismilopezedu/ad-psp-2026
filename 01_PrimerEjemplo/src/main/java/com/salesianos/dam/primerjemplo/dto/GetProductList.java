package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "GetProductList",
        description = "Item de una lista de productos"
)
public record GetProductList(
        @Schema(
                description = "Identificador único del producto",
                example = "15"
        )
        Long id,

        @Schema(
                description = "Nombre del producto",
                example = "Camiseta"
        )
        String name,

        @Schema(
                description = "Precio del producto",
                example = "19.99"
        )
        Double price
) {

    public static GetProductList of(Product p) {
        return new GetProductList(p.getId(), p.getName(), p.getPrice());
    }




}

