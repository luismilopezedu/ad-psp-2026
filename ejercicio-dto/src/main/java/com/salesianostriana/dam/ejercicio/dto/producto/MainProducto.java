package com.salesianostriana.dam.ejercicio.dto.producto;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MainProducto {

    //@PostConstruct
    public void main() {

        Categoria categoria = Categoria
                .builder()
                .id(1L)
                .nombre("Ordenadores")
                .build();

        Producto p1 = Producto.builder()
                .id(1L)
                .nombre("Portátil")
                .pvp(999.99)
                .categoria(categoria)
                .imagenes(List.of("Imagen 1", "Imagen 2", "Imagen 3"))
                .build();

        Producto p2 = Producto.builder()
                .id(2L)
                .nombre("Sobremesa pepino")
                .pvp(1999.99)
                .categoria(categoria)
                .build();

        Producto p3 = null;

        List<Producto> productos = new ArrayList<>(List.of(p1, p2));

        productos.stream()
                .map(ProductoDTO::of)
                .forEach(System.out::println);


        System.out.println(ProductoDTO.of(p3));

    }

}
