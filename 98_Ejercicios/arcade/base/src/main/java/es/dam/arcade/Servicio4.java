package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio4 {

    public Tipos.Detalle join(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Detalle derivada(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Detalle declarada(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Programa programa(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<Tipos.Programa> programas(String nombre) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
