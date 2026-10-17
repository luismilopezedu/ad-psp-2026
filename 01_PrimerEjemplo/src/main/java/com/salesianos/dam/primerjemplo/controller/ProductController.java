package com.salesianos.dam.primerjemplo.controller;

import com.salesianos.dam.primerjemplo.dto.EditProductDto;
import com.salesianos.dam.primerjemplo.dto.GetProductDetail;
import com.salesianos.dam.primerjemplo.dto.GetProductList;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import com.salesianos.dam.primerjemplo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    /*
    @Operation(
    summary = "Obtener todos los productos",
    description = "Permite obtener todos los productos del catálogo"
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Listado de productos del catálogo",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(
                schema = @Schema(
                    implementation = GetProductList.class
                )
            ),
            examples = @ExampleObject(
                name = "Listado de productos",
                summary = "Ejemplo de catálogo con varios productos",
                value = """
                    [
                      {
                        "id": 15,
                        "name": "Camiseta",
                        "price": 19.99
                      },
                      {
                        "id": 16,
                        "name": "Pantalón",
                        "price": 39.99
                      },
                      {
                        "id": 17,
                        "name": "Zapatillas",
                        "price": 59.95
                      }
                    ]
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "No se han encontrado productos",
        content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(
                implementation = ProblemDetail.class
            )
        )
    )
})
@GetMapping
public ResponseEntity<List<GetProductList>> getAllProducts() {

    List<Product> result = productService.getAllProducts();

    return ResponseEntity.ok(
        result.stream()
              .map(GetProductList::of)
              .toList()
    );
}

     */

    @Operation(
            summary = "Obtener todos los productos",
            description = "Permite obtener todos los productos del catálogo"
    )
    @ApiResponses(
            {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de productos del catálogo",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(
                                                    implementation = GetProductList.class
                                            )
                                    ),
                                    examples = @ExampleObject(
                                            name = "Listado de productos",
                                            summary = "Ejemplo de catálogo con varios productos",
                                            value = """
                    [
                      {
                        "id": 15,
                        "name": "Camiseta",
                        "price": 19.99
                      },
                      {
                        "id": 16,
                        "name": "Pantalón",
                        "price": 39.99
                      },
                      {
                        "id": 17,
                        "name": "Zapatillas",
                        "price": 59.95
                      }
                    ]
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No hay productos con esos criterios de búsqueda",
                            content = @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(
                                            implementation = ProblemDetail.class
                                    ),
                                    examples = @ExampleObject(
                                            name = "Error 404",
                                            summary = "Mensaje de error informando de que no hay productos",
                                            value = """
                                                    
                                                    {
                                                      "detail": "Products not found",
                                                      "instance": "/api/product",
                                                      "status": 404,
                                                      "title": "Recurso no encontrado",
                                                      "type": "https://example.com/errors/product-not-found"
                                                    }
                                                    
                                                    """
                                    )
                            )
                    )
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

    @GetMapping("/category/{id}")
    public ResponseEntity<List<GetProductList>> getProductByCategory(@PathVariable Long id) {

        List<Product> result = productService.getProductByCategory(id);
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
