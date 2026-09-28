package com.salesianos.dam.primerjemplo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public interface ProductRepository
    extends JpaRepository<Product, Long> {}

/*@Repository
public class ProductRepository {

    private List<Product> products;

    public ProductRepository() {
        this.products = new ArrayList<>();
    }

    public Product addProduct(Product product) {
        products.add(product);
        return product;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Optional<Product> getProductByName(String name) {
        return products.stream()
                .filter(p -> p.name().equals(name))
                .findFirst();
    }

    public Product updateProduct(Product product) {

        products.removeIf(p -> p.name().equals(product.name()));
        addProduct(product);
        return product;
    }

    public void deleteProduct(String name) {
        products.removeIf(p -> p.name().equals(name));
    }


    public List<Product> filterProducts(String name, String price) {
        return products.stream()
                .filter(p -> p.name().equals(name))
                .filter(p -> p.price().equals(price))
                .toList();
    }

}*/
