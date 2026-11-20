package es.dam.arcade;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class Servicio2 {

    public String plataforma(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public List<String> logros(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public void retirar(String id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public long visibles() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public Map<String,Object> auditoria() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
