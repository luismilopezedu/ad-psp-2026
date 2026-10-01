package com.salesianos.dam.primerjemplo.service;

import com.salesianos.dam.primerjemplo.error.ProductNotFoundException;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /*
        RESPONSABILIDADES DE ESTE MÉTODO
            - Recoger la lista de productos del repositorio
            - Comprobar si está vacía, y en tal caso, lanzar
              una excepción
            - Devolverla si tiene datos
     */
    public List<Product> getAllProducts() {
        List<Product> result = productRepository.findAll();
        if (result.isEmpty()) {
            // return ResponseEntity.status(404).build();
            //return ResponseEntity.notFound().build();

            // Después lo cambiamos por una excepción personalizada
            throw new ProductNotFoundException();
        }
        return result;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }


}
