package com.salesianostriana.dam.ejercicio.dto.alumno;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    private Long id;
    private String nombre;
    private String tipo;
    private String tutor;
    private String aula;

}
