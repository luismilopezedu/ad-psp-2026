package com.salesianos.dam.primerjemplo.error;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Products not found");
    }

    public  ProductNotFoundException(Long id) {
        super("Product not found with id " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
