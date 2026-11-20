package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio1 {

    public Ficha alta(Tipos.Alta alta) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Ficha ficha(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> juegos() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> plataformas() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public long total() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
