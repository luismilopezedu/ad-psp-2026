package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api1 {
    private final Servicio1 servicio;
    public Api1(Servicio1 servicio) { this.servicio = servicio; }

    @PostMapping("/juegos")
    @ResponseStatus(HttpStatus.CREATED)
    public Ficha alta(@RequestBody Tipos.Alta alta) {
        return servicio.alta(alta);
    }

    @GetMapping("/juegos/{id}")
    public Ficha ficha(@PathVariable String id) {
        return servicio.ficha(id);
    }

    @GetMapping("/juegos")
    public List<String> juegos() {
        return servicio.juegos();
    }

    @GetMapping("/plataformas")
    public List<String> plataformas() {
        return servicio.plataformas();
    }

    @GetMapping("/inventario/total")
    public long total() {
        return servicio.total();
    }
}
