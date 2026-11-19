package es.dam.aula;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class Etapa3Controller {

    @GetMapping("/lectura/curso/{id}")
    public Object cursoCompleto(@PathVariable String id, @RequestParam(defaultValue = "join") String plan) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/lectura/area/{id}")
    public Object programa(@PathVariable String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/lectura/programas")
    public Object programas(@RequestParam String nombre) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
