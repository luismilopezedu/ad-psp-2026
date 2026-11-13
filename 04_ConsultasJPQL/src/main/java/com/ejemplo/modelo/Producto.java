package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(nullable = false)
 private String nombre;
 @Column(precision = 12, scale = 2)
 private BigDecimal precio;
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "categoria_id", nullable = false)
 private Categoria categoria;
 protected Producto() {}
 public Producto(String nombre, BigDecimal precio, Categoria categoria) {
  this.nombre = nombre;
  this.precio = precio;
  this.categoria = categoria;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public BigDecimal getPrecio() { return precio; }
 public Categoria getCategoria() { return categoria; }
 @Override public String toString() { return "Producto[id=" + id + ", nombre=" + nombre + ", precio=" + precio + "]"; }
}
