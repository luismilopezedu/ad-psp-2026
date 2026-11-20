package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api7 {
    private final Servicio7 servicio;
    public Api7(Servicio7 servicio) { this.servicio = servicio; }

    @GetMapping("/buscar/rapida")
    public Tipos.Pagina rapida() {
        return servicio.rapida();
    }

    @PostMapping("/buscar/avanzada")
    public Tipos.Pagina avanzada() {
        return servicio.avanzada();
    }

    @GetMapping("/buscar/flexible")
    public Tipos.Pagina flexible() {
        return servicio.flexible();
    }

    @GetMapping("/buscar/puzles")
    public Tipos.Pagina puzles(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.puzles(page, size);
    }

    @GetMapping("/buscar/aventuras-premium")
    public Tipos.Pagina aventuras(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.aventuras(page, size);
    }
}
