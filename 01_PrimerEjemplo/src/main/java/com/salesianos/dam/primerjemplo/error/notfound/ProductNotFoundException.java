package com.salesianos.dam.primerjemplo.error.notfound;

public class ProductNotFoundException extends EntityNotFoundException {

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
