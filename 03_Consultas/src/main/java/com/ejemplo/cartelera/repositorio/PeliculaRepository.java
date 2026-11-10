package com.ejemplo.cartelera.repositorio;

import com.ejemplo.cartelera.modelo.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de partida: ¡todavía no contiene métodos de consultas derivadas!
 *
 * En la actividad guiada se añadirán los tres primeros métodos de búsqueda.
 * Después, cada pareja implementará otros cuatro sin escribir SQL ni @Query.
 */
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    // ACTIVIDAD 3 (guiada): añade aquí los métodos del tutorial.

    // ACTIVIDAD 4 (parejas): añade aquí los métodos de los cuatro retos.
}
