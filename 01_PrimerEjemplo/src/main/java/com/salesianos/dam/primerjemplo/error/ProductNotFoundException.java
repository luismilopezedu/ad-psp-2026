package com.salesianos.dam.primerjemplo.error;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Products not found");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
