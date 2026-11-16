package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "etiquetas")
public class Etiqueta {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private String nombre;
 protected Etiqueta() {}
 public Etiqueta(String nombre) {
  this.nombre=nombre;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }

}
