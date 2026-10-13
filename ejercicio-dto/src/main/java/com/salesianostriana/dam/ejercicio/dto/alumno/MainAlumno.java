package com.salesianostriana.dam.ejercicio.dto.alumno;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MainAlumno {

    @PostConstruct
    public void main() {

        Curso curso = Curso.builder()
                .id(1L)
                .nombre("2º DAM")
                .tipo("Grado Superior")
                .tutor("Luismi")
                .aula("Aula 1")
                .build();

        Alumno alumno = Alumno.builder()
                .id(2L)
                .nombre("José")
                .apellido1("Pérez")
                .apellido2("García")
                .email("jose.perez@triana.salesianos.edu")
                .telefono("954331488")
                //.curso(curso)
                .direccion(
                        Direccion.builder()
                                .id(3L)
                                .tipoVia("Calle")
                                .linea1("Condes de Bustillo 17")
                                .cp("41010")
                                .poblacion("Sevilla")
                                .provincia("Sevilla")
                                .build()
                )
                .build();


        System.out.println("Alumno: " + AlumnoDTO.of(alumno));

    }

}
