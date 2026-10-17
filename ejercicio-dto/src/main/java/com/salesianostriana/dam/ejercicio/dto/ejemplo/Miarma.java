package com.salesianostriana.dam.ejercicio.dto.ejemplo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Miarma {

    private Long id;
    private String nombre;
    private int edad;

}
