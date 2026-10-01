package com.salesianos.dam.primerjemplo.controller;

import com.salesianos.dam.primerjemplo.dto.EditProductDto;
import com.salesianos.dam.primerjemplo.dto.GetProductDetail;
import com.salesianos.dam.primerjemplo.dto.GetProductList;
import com.salesianos.dam.primerjemplo.error.ProductNotFoundException;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import com.salesianos.dam.primerjemplo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @PostMapping
    //public ResponseEntity<Product> addProduct(@RequestBody Product product) {
    public ResponseEntity<GetProductDetail> addProduct(@RequestBody EditProductDto product) {

        if (StringUtils.hasText(product.name())) {
            return ResponseEntity.status(201)
                    .body(
                            GetProductDetail.of(
                                    productRepository.save(product.to())
                            )
                    );
        }

        return ResponseEntity.badRequest().build();

    }

    @GetMapping
    //public ResponseEntity<List<Product>> getAllProducts() {
    public ResponseEntity<List<GetProductList>> getAllProducts() {

        /*
            RESPONSABILIDADES DE ESTE MÉTODO
                - Invocar al servicio para recibir la lista
                  de productos.
                - Transformar los productos al dto de salida.
                - Devolver la respuesta 200 OK con los productos.
         */

        List<Product> result = productService.getAllProducts();
        return ResponseEntity.ok(
                result
                        .stream()
                        .map(GetProductList::of)
                        .toList());
    }

    @GetMapping("/{id}")
    //public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    public ResponseEntity<GetProductDetail> getProductById(@PathVariable Long id) {

        /*return ResponseEntity.of(
                productRepository.findById(id)
                        .map(GetProductDetail::of)
        );*/

        /*return ResponseEntity.ok(
                Optional.ofNullable(productService.getProductById(id))
                        .map(GetProductDetail::of)
                        .get()
        );*/

        return ResponseEntity.ok(GetProductDetail.of(productService.getProductById(id)));

    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProductDetail> updateProduct(
            @PathVariable Long id,
            @RequestBody EditProductDto product) {


        if (!StringUtils.hasText(product.name()) || product.price() < 0) {
            return ResponseEntity.badRequest().build();
        }

        return productRepository.findById(id)
                .map(p -> {
                    p.setName(product.name());
                    p.setPrice(product.price());
                    p.setDetails(product.details());
                    return ResponseEntity.ok(
                            GetProductDetail.of(productRepository.save(p))
                    );
                }).orElse(ResponseEntity.notFound().build());


    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
