package es.dam.aula;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class Etapa1Controller {

    @PostMapping("/cursos")
    @ResponseStatus(HttpStatus.CREATED)
    public Object alta(@RequestBody Vistas.Alta alta) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/cursos/{id}")
    public Object detalleBasico(@PathVariable String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping("/cursos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
