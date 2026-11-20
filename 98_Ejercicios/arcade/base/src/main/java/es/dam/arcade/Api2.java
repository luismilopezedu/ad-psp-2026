package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api2 {
    private final Servicio2 servicio;
    public Api2(Servicio2 servicio) { this.servicio = servicio; }

    @GetMapping("/juegos/{id}/plataforma")
    public String plataforma(@PathVariable String id) {
        return servicio.plataforma(id);
    }

    @GetMapping("/juegos/{id}/logros")
    public List<String> logros(@PathVariable String id) {
        return servicio.logros(id);
    }

    @DeleteMapping("/juegos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable String id) {
        servicio.retirar(id);
    }

    @GetMapping("/archivo/visibles")
    public long visibles() {
        return servicio.visibles();
    }

    @GetMapping("/archivo/auditoria-j1")
    public Map<String,Object> auditoria() {
        return servicio.auditoria();
    }
}
