package com.salesianos.dam.primerjemplo.controller;

import com.salesianos.dam.primerjemplo.dto.EditProductDto;
import com.salesianos.dam.primerjemplo.dto.GetProductDetail;
import com.salesianos.dam.primerjemplo.dto.GetProductList;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;

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

        List<Product> result = productRepository.findAll();
        if (result.isEmpty()) {
            // return ResponseEntity.status(404).build();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                result
                        .stream()
                        .map(GetProductList::of)
                        .toList());
    }

    @GetMapping("/{id}")
    //public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    public ResponseEntity<GetProductDetail> getProductById(@PathVariable Long id) {

        return ResponseEntity.of(
                productRepository.findById(id)
                        .map(GetProductDetail::of)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return productRepository.findById(id)
                .map(p -> {
                    p.setName(product.getName());
                    p.setPrice(product.getPrice());
                    return ResponseEntity.ok(productRepository.save(p));
                })
                .orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
