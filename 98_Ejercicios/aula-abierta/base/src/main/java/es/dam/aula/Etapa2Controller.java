package es.dam.aula;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
public class Etapa2Controller {

    @GetMapping("/catalogo/nivel")
    public Object porNivel(@RequestParam String nivel) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/catalogo/titulo")
    public Object porTitulo(@RequestParam String texto) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/catalogo/prioridad")
    public Object prioridad(@RequestParam String preferente, @RequestParam String alternativo, @RequestParam int minimo) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/catalogo/resumen")
    public Object resumen(@RequestParam List<String> ids) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
