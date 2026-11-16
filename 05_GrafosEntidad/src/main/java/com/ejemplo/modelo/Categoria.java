package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categorias")
public class Categoria {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(nullable = false, unique = true)
 private String nombre;
 protected Categoria() {}
 public Categoria(String nombre) {
  this.nombre = nombre;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 @Override public String toString() { return "Categoria[id=" + id + ", nombre=" + nombre + "]"; }
}
