package com.salesianostriana.dam.ejercicio.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    private Long id;
    private String nombre;
    private String desc;
    private double pvp;
    @Builder.Default
    private List<String> imagenes = new ArrayList<>();
    private Categoria categoria;

}
