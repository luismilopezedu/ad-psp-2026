package com.salesianos.dam.primerjemplo.repo;

import com.salesianos.dam.primerjemplo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository
    extends JpaRepository<Product, Long> {

    boolean existsByName(String name);
    List<Product> findByCategoryId(Long id);
}

