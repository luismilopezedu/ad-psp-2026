package com.salesianos.dam.primerjemplo.dto;

import com.salesianos.dam.primerjemplo.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "GetProductDetail",
        description = "Detalle de un producto"
)
public record GetProductDetail(
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
        Double price,
        @Schema(
                description = "Detalles del producto",
                example = "Camiseta de algodón"
        )
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
