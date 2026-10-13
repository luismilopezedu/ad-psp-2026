package com.salesianos.dam.primerjemplo.controller;

import com.salesianos.dam.primerjemplo.dto.EditProductDto;
import com.salesianos.dam.primerjemplo.dto.GetProductDetail;
import com.salesianos.dam.primerjemplo.dto.GetProductList;
import com.salesianos.dam.primerjemplo.error.ProductNotFoundException;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import com.salesianos.dam.primerjemplo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name="Product Controller", description = "Un controlador de productos")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<GetProductDetail> addProduct(@RequestBody EditProductDto product) {

        return ResponseEntity.status(201)
                .body(GetProductDetail.of(productService.addProduct(product)));

    }

    @Operation(
            summary = "Obtener todos los productos",
            description = "Permite obtener todos los productos del catálogo"
    )
    @ApiResponses(
            {
                    @ApiResponse(description = "Listado de productos del catálogo",
                            responseCode = "200"),
                    @ApiResponse(description = "Error - no hay productos con los criterios de búsqueda",
                            responseCode = "404")
            }
    )
    @GetMapping
    public ResponseEntity<List<GetProductList>> getAllProducts() {

        List<Product> result = productService.getAllProducts();
        return ResponseEntity.ok(
                result
                        .stream()
                        .map(GetProductList::of)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductDetail> getProductById(@PathVariable Long id) {

        return ResponseEntity.ok(GetProductDetail.of(productService.getProductById(id)));

    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProductDetail> updateProduct(
            @PathVariable Long id,
            @RequestBody EditProductDto product) {

        return ResponseEntity.ok(
                GetProductDetail.of(productService.updateProduct(id, product)));


    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
