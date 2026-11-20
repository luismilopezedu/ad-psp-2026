package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio5 {

    public String modo() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public String moneda() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public int limite() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Map<String,Object> resumen() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Map<String,Object> importe(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
