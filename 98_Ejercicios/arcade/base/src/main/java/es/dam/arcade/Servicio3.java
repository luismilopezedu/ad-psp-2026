package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio3 {

    public List<String> genero(String genero) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> titulo(String texto) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> prioridad(String preferente, String alternativo, int minimo) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> seleccion(List<String> ids) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<Ficha> fichas(String genero) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
