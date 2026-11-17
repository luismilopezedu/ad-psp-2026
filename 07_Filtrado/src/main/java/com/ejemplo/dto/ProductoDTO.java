package com.ejemplo.dto;
import java.math.BigDecimal;

public class ProductoDTO {
 private final Long id;
 private final String nombre;
 private final BigDecimal precio;
 private final Boolean activo;
 private final String nombreCategoria;
 public ProductoDTO(Long id, String nombre, BigDecimal precio, Boolean activo, String nombreCategoria) {
  this.id = id;
  this.nombre = nombre;
  this.precio = precio;
  this.activo = activo;
  this.nombreCategoria = nombreCategoria;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public BigDecimal getPrecio() { return precio; }
 public Boolean getActivo() { return activo; }
 public String getNombreCategoria() { return nombreCategoria; }
}
