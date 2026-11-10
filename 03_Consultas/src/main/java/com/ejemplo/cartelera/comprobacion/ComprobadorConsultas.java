package com.ejemplo.cartelera.comprobacion;

import com.ejemplo.cartelera.modelo.Pelicula;
import com.ejemplo.cartelera.repositorio.PeliculaRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Punto de entrada para practicar consultas derivadas SIN controladores REST.
 *
 * 1) Declara primero el método en PeliculaRepository.
 * 2) Descomenta la llamada correspondiente de esta clase.
 * 3) Ejecuta la aplicación y observa los resultados y el SQL en consola.
 *
 * No se proporcionan los nombres de los métodos de los retos de parejas.
 */
@Component
public class ComprobadorConsultas {

    private final PeliculaRepository peliculaRepository;

    public ComprobadorConsultas(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ejecutar() {
        System.out.println("\n========== CARTELERA: CONSULTAS DERIVADAS ==========");
        mostrarNumero("Películas cargadas (deben ser 12)", peliculaRepository.count());

        // =============================================================
        // ACTIVIDAD 3: PROGRAMACIÓN GUIADA
        // Primero declara cada método en PeliculaRepository.
        // Después quita las dos barras // de su llamada de abajo.
        // =============================================================

        // PASO 1. Deberían aparecer 4 películas de Acción.
        // mostrarPeliculas("Películas de Acción", peliculaRepository.findByGenero("Acción"));

        // PASO 2. Deberían aparecer 4 películas de más de 120 minutos.
        // mostrarPeliculas("Más de 120 minutos", peliculaRepository.findByDuracionMinutosGreaterThan(120));

        // PASO 3. Debería aparecer Código rojo.
        // mostrarPeliculas("Título que contiene CÓDIGO", peliculaRepository.findByTituloContainingIgnoreCase("CÓDIGO"));

        // =============================================================
        // ACTIVIDAD 4: RETOS EN PAREJAS
        // Sustituye TU_METODO por el método que hayas creado.
        // Las llamadas quedan comentadas hasta que termines cada reto.
        // =============================================================

        // RETO 1. Género Acción y duración <= 105. Esperado: 2 películas.
        // mostrarPeliculas("Reto 1", peliculaRepository.TU_METODO("Acción", 105));

        // RETO 2. El título contiene "código" (sin diferenciar mayúsculas).
        // mostrarPeliculas("Reto 2", peliculaRepository.TU_METODO("código"));

        // RETO 3. Disponibles por título ascendente. Esperado: 9 películas.
        // mostrarPeliculas("Reto 3", peliculaRepository.TU_METODO());

        // RETO 4. Contar películas de Acción. Esperado: 4.
        // mostrarNumero("Reto 4", peliculaRepository.TU_METODO("Acción"));

        // AMPLIACIÓN: las tres películas disponibles de mayor duración.
        // mostrarPeliculas("Ampliación", peliculaRepository.TU_METODO());

        System.out.println("========== FIN DE LAS COMPROBACIONES ==========\n");
    }

    private void mostrarPeliculas(String titulo, List<Pelicula> peliculas) {
        System.out.printf("\n[%s] Coincidencias: %d%n", titulo, peliculas.size());
        peliculas.forEach(pelicula -> System.out.printf(
                "  • %s | %s | %d min | disponible=%s | estreno=%s%n",
                pelicula.getTitulo(),
                pelicula.getGenero(),
                pelicula.getDuracionMinutos(),
                pelicula.isDisponible(),
                pelicula.getFechaEstreno()
        ));
    }

    private void mostrarNumero(String titulo, long numero) {
        System.out.printf("[%s] %d%n", titulo, numero);
    }
}
