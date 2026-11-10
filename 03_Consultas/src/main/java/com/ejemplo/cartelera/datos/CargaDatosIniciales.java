package com.ejemplo.cartelera.datos;

import com.ejemplo.cartelera.repositorio.PeliculaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Inserta los 12 registros de ejemplo después de crear el repositorio.
 * Solo carga datos si la tabla está vacía.
 */
@Component
public class CargaDatosIniciales {

    private final PeliculaRepository peliculaRepository;

    public CargaDatosIniciales(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @PostConstruct
    public void cargar() {
        if (peliculaRepository.count() == 0) {
            peliculaRepository.saveAll(DatosPeliculas.crearPeliculas());
            System.out.println("[DATOS] Se han cargado 12 películas de ejemplo.");
        } else {
            System.out.println("[DATOS] Ya hay películas; no se duplican los datos.");
        }
    }
}
