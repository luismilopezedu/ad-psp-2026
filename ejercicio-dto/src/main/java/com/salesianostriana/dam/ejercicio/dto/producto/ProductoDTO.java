package com.salesianostriana.dam.ejercicio.dto.producto;

import org.springframework.util.CollectionUtils;

import java.util.Objects;

public record ProductoDTO(
        String nombre,
        Double pvp,
        String imagen,
        String categoria
) {

    public static ProductoDTO of(Producto producto) {
        if (producto == null) {
            return null;
        }
        return new ProductoDTO(
                producto.getNombre(),
                producto.getPvp(),
                CollectionUtils.isEmpty(producto.getImagenes()) ? null : producto.getImagenes().get(0),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null

        );
    }

}
