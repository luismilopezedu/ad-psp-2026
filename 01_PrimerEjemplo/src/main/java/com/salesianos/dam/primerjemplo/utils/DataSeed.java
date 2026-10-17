package com.salesianos.dam.primerjemplo.utils;

import com.salesianos.dam.primerjemplo.model.Category;
import com.salesianos.dam.primerjemplo.model.Product;
import com.salesianos.dam.primerjemplo.repo.CategoryRepository;
import com.salesianos.dam.primerjemplo.repo.ProductRepository;
import com.salesianos.dam.primerjemplo.service.CategoryService;
import com.salesianos.dam.primerjemplo.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeed {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @PostConstruct
    public void initData() {

        /*
            Invocar a los servicios para insertar
            datos de ejemplo
         */

        Category c1 = Category.builder()
                .name("Electrónica")
                .build();

        Category c2 = Category.builder()
                .name("Alimentación")
                .build();

        categoryRepository.saveAll(List.of(c1,c2));


        Product p1 = Product.builder()
                .name("TV 65")
                .price(499.99)
                .details("Smart TV 65")
                .category(c1)
                .build();

        productRepository.save(p1);


    }

}
