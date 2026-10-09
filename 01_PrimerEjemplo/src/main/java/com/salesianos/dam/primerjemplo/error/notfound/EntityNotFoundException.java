package com.salesianos.dam.primerjemplo.error.notfound;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
