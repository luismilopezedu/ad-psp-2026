package com.salesianos.dam.primerjemplo.repo;

import com.salesianos.dam.primerjemplo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository
    extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

}

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
    extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long id);
}

