package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio7 {

    public Tipos.Pagina rapida() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina avanzada() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina flexible() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina puzles(int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina aventuras(int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
