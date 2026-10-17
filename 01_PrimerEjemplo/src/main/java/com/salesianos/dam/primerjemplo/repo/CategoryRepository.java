package com.salesianos.dam.primerjemplo.repo;

import com.salesianos.dam.primerjemplo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
