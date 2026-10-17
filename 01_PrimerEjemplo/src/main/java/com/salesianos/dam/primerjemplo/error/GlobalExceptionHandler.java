package com.salesianos.dam.primerjemplo.error;

import com.salesianos.dam.primerjemplo.error.notfound.CategoryNotFoundException;
import com.salesianos.dam.primerjemplo.error.notfound.EntityNotFoundException;
import com.salesianos.dam.primerjemplo.error.notfound.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleProductNotFound(EntityNotFoundException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://example.com/errors/not-found"));

        return problem;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Categoría no encontrada");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://example.com/errors/category-not-found"));

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