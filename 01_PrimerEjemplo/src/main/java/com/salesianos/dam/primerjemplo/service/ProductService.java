package com.salesianos.dam.primerjemplo.service;

import com.salesianos.dam.primerjemplo.dto.EditProductDto;
import com.salesianos.dam.primerjemplo.error.InvalidProductException;
import com.salesianos.dam.primerjemplo.error.notfound.ProductNotFoundException;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            throw new ProductNotFoundException();
        }
        return result;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product addProduct(EditProductDto editProductDto) {
        validateProduct(editProductDto);
        return productRepository.save(editProductDto.to());
    }

    public Product updateProduct(Long id, EditProductDto editProductDto) {

        return productRepository.findById(id)
                .map(p -> {
                            validateProduct(editProductDto);
                            p.setName(editProductDto.name());
                            p.setPrice(editProductDto.price());
                            p.setDetails(editProductDto.details());
                            return productRepository.save(p);
                        })
                .orElseThrow(() -> new ProductNotFoundException(id));
    }


    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    public void deleteProduct(Long id) {
        // Si queremos que sea NO IDEMPOTENTE
        // hay que descomentar las dos siguientes líneas de código
        //if (!productRepository.existsById(id))
        //    throw new ProductNotFoundException(id);
        productRepository.deleteById(id);
    }


    private void validateProduct(EditProductDto editProductDto) {
        if (!StringUtils.hasText(editProductDto.name())
                || !StringUtils.hasText(editProductDto.details())) {
            throw new InvalidProductException("Product name and details are required");
        }
        if (editProductDto.price() < 0) {
            throw new InvalidProductException("Price is required");
        }
    }




}
