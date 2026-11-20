package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api3 {
    private final Servicio3 servicio;
    public Api3(Servicio3 servicio) { this.servicio = servicio; }

    @GetMapping("/catalogo/genero")
    public List<String> genero(@RequestParam String genero) {
        return servicio.genero(genero);
    }

    @GetMapping("/catalogo/titulo")
    public List<String> titulo(@RequestParam String texto) {
        return servicio.titulo(texto);
    }

    @GetMapping("/catalogo/prioridad")
    public List<String> prioridad(@RequestParam String preferente, @RequestParam String alternativo, @RequestParam int minimo) {
        return servicio.prioridad(preferente, alternativo, minimo);
    }

    @GetMapping("/catalogo/seleccion")
    public List<String> seleccion(@RequestParam List<String> ids) {
        return servicio.seleccion(ids);
    }

    @GetMapping("/catalogo/fichas")
    public List<Ficha> fichas(@RequestParam String genero) {
        return servicio.fichas(genero);
    }
}
