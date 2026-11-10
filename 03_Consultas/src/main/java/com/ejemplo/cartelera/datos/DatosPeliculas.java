package com.ejemplo.cartelera.datos;

import com.ejemplo.cartelera.modelo.Pelicula;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos compartidos con el tutorial de la sesión.
 * No modifica el repositorio ni resuelve los retos del alumnado.
 */
public final class DatosPeliculas {

    private DatosPeliculas() {
    }

    public static List<Pelicula> crearPeliculas() {
        return List.of(
                new Pelicula("La órbita perdida", "Ciencia ficción", 125, true, LocalDate.of(2023, 4, 21)),
                new Pelicula("Rumbo al norte", "Aventuras", 95, true, LocalDate.of(2020, 8, 14)),
                new Pelicula("Sombras de invierno", "Suspense", 110, false, LocalDate.of(2022, 11, 3)),
                new Pelicula("Código rojo", "Acción", 105, true, LocalDate.of(2024, 5, 10)),
                new Pelicula("Última frontera", "Acción", 130, false, LocalDate.of(2021, 9, 17)),
                new Pelicula("El gran viaje", "Aventuras", 140, true, LocalDate.of(2025, 2, 7)),
                new Pelicula("Misión Delta", "Acción", 90, true, LocalDate.of(2026, 1, 15)),
                new Pelicula("A ritmo de jazz", "Musical", 102, true, LocalDate.of(2019, 6, 28)),
                new Pelicula("Crónicas del mar", "Drama", 115, false, LocalDate.of(2023, 12, 1)),
                new Pelicula("La última escena", "Drama", 85, true, LocalDate.of(2020, 1, 10)),
                new Pelicula("Acción en la sombra", "Acción", 145, true, LocalDate.of(2025, 7, 18)),
                new Pelicula("Recuerdos de abril", "Drama", 100, true, LocalDate.of(2024, 4, 12))
        );
    }
}
