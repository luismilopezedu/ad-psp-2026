package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(nullable = false)
 private String nombre;
 @Column(precision = 12, scale = 2)
 private BigDecimal precio;
 private Boolean activo;
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "categoria_id", nullable = false)
 private Categoria categoria;
 protected Producto() {}
 public Producto(String nombre, BigDecimal precio, Categoria categoria) {
  this(nombre, precio, true, categoria);
 }
 public Producto(String nombre, BigDecimal precio, Boolean activo, Categoria categoria) {
  this.activo = activo;
  this.nombre = nombre;
  this.precio = precio;
  this.categoria = categoria;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public BigDecimal getPrecio() { return precio; }
 public Boolean getActivo() { return activo; }
 public void setNombre(String nombre) { this.nombre = nombre; }
 public void setPrecio(BigDecimal precio) { this.precio = precio; }
 public void setActivo(Boolean activo) { this.activo = activo; }
 public void setCategoria(Categoria categoria) { this.categoria = categoria; }
 public Categoria getCategoria() { return categoria; }
 @Override public String toString() { return "Producto[id=" + id + ", nombre=" + nombre + ", precio=" + precio + "]"; }
}
