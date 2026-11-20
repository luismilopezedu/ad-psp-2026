package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api4 {
    private final Servicio4 servicio;
    public Api4(Servicio4 servicio) { this.servicio = servicio; }

    @GetMapping("/lectura/join/{id}")
    public Tipos.Detalle join(@PathVariable String id) {
        return servicio.join(id);
    }

    @GetMapping("/lectura/derivada/{id}")
    public Tipos.Detalle derivada(@PathVariable String id) {
        return servicio.derivada(id);
    }

    @GetMapping("/lectura/declarada/{id}")
    public Tipos.Detalle declarada(@PathVariable String id) {
        return servicio.declarada(id);
    }

    @GetMapping("/programas/{id}")
    public Tipos.Programa programa(@PathVariable String id) {
        return servicio.programa(id);
    }

    @GetMapping("/programas")
    public List<Tipos.Programa> programas(@RequestParam String nombre) {
        return servicio.programas(nombre);
    }
}
