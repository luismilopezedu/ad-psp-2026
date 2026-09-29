package com.salesianos.dam.primerjemplo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
                if (StringUtils.hasText(product.getName())) {
            return ResponseEntity.status(201)
                    .body(productRepository.save(product));
        }

        return ResponseEntity.badRequest().build();

    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> result = productRepository.findAll();
        if (result.isEmpty()) {
            // return ResponseEntity.status(404).build();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.of(productRepository.findById(id));
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

}
//
//    @PostMapping
//    //public Product addProduct(@RequestBody Product product) {
//    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
//
//        if (StringUtils.hasText(product.name())
//                && StringUtils.hasText(product.price())) {
//            return ResponseEntity.status(201)
//                    .body(productRepository.addProduct(product));
//        }
//
//        return ResponseEntity.badRequest().build();
//
//
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Product>> getProducts() {
//        List<Product> result = productRepository.getProducts();
//        if (result.isEmpty()) {
//            // return ResponseEntity.status(404).build();
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(result);
//
//    }
//
//    // /product/search?name=XXX&price=ZZZ
//    @GetMapping("/search")
//    public ResponseEntity<List<Product>> getFilteredProducts(
//            @RequestParam(value = "name") String name,
//            @RequestParam("price") String price
//            //@RequestParam Map<String, String> params
//    ) {
//        // Sustituir esta línea por la llamada a un método
//        // de filtrado
//        List<Product> result = productRepository.filterProducts(name, price);
//
//        if (result.isEmpty()) {
//            // return ResponseEntity.status(404).build();
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(result);
//
//    }
//
//    @GetMapping("/{name}")
//    public ResponseEntity<Product> getProduct(@PathVariable String name) {
//        return ResponseEntity.of(productRepository.getProductByName(name));
//    }
//
//    @PutMapping("/{name}")
//    public ResponseEntity<Product> updateProduct(@PathVariable String name,
//                                                 @RequestBody Product product) {
//
//        if (productRepository.getProductByName(name).isEmpty())
//            return ResponseEntity.notFound().build();
//
//        if (StringUtils.hasText(product.name())
//                && StringUtils.hasText(product.price())) {
//            return ResponseEntity.status(200)
//                    .body(productRepository.addProduct(product));
//        }
//
//        return ResponseEntity.badRequest().build();
//    }
//
//    @DeleteMapping("/{name}")
//    public ResponseEntity<Void> deleteProduct(@PathVariable String name) {
//
//        // Enfoque idempotente
//        /*
//        productRepository.deleteProduct(name);
//        return ResponseEntity.noContent().build();
//        */
//
//        // Enfoque no idempotente
//        if (productRepository.getProductByName(name).isEmpty())
//            return ResponseEntity.notFound().build();
//
//        productRepository.deleteProduct(name);
//        return ResponseEntity.noContent().build();
//    }
//
//
//}
