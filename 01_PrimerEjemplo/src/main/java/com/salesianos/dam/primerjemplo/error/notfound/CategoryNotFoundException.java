package com.salesianos.dam.primerjemplo.error.notfound;

public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException() {
        super("Categories not found");
    }
}
