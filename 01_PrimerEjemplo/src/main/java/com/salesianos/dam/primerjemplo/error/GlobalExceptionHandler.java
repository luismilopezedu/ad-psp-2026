package com.salesianos.dam.primerjemplo.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://example.com/errors/product-not-found"));

        return problem;
    }

    @ExceptionHandler(InvalidProductException.class)
    public ProblemDetail handleInvalidProduct(InvalidProductException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Datos de producto inválidos");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://example.com/errors/invalid-product"));

        return problem;
    }


}