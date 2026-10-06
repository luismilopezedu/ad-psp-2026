package com.salesianos.dam.primerjemplo.model;

/*record Product(String name, String price){}*/

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id @GeneratedValue
    private Long id;
    private String name;
    private Double price;
    private String details;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    @ManyToOne
    private Category category;


    /*
        TIPOS DE ASOCIACIONES

        ManyToOne: Product -> Category
        OneToMany: Category ->>> Product
        ManyToMany: Product <<<-->>> Tag
        OneToOne: Product -> ProductInfo

     */



}

