package es.dam.arcade;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class Api5 {
    private final Servicio5 servicio;
    public Api5(Servicio5 servicio) { this.servicio = servicio; }

    @GetMapping("/config/modo")
    public String modo() {
        return servicio.modo();
    }

    @GetMapping("/config/moneda")
    public String moneda() {
        return servicio.moneda();
    }

    @GetMapping("/config/limite")
    public int limite() {
        return servicio.limite();
    }

    @GetMapping("/config/resumen")
    public Map<String,Object> resumen() {
        return servicio.resumen();
    }

    @GetMapping("/config/importe/{id}")
    public Map<String,Object> importe(@PathVariable String id) {
        return servicio.importe(id);
    }
}
