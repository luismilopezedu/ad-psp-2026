package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio6 {

    public Tipos.Pagina todos(int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina plataforma(String id, int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina genero(String genero, int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina precio(int minimo, int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Tipos.Pagina logro(String nombre, int page, int size) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
