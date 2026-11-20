package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api6 {
    private final Servicio6 servicio;
    public Api6(Servicio6 servicio) { this.servicio = servicio; }

    @GetMapping("/paginas/juegos")
    public Tipos.Pagina todos(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.todos(page, size);
    }

    @GetMapping("/paginas/plataforma")
    public Tipos.Pagina plataforma(@RequestParam String id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.plataforma(id, page, size);
    }

    @GetMapping("/paginas/genero")
    public Tipos.Pagina genero(@RequestParam String genero, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.genero(genero, page, size);
    }

    @GetMapping("/paginas/precio")
    public Tipos.Pagina precio(@RequestParam int minimo, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.precio(minimo, page, size);
    }

    @GetMapping("/paginas/logro")
    public Tipos.Pagina logro(@RequestParam String nombre, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="2") int size) {
        return servicio.logro(nombre, page, size);
    }
}
