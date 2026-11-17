package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(nullable = false)
 private String nombre;
 @Column(nullable = false, unique = true)
 private String email;
 @Column(nullable = false)
 private boolean activo;
 protected Cliente() {}
 public Cliente(String nombre, String email, boolean activo) {
  this.nombre = nombre;
  this.email = email;
  this.activo = activo;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public String getEmail() { return email; }
 public boolean isActivo() { return activo; }
 @Override public String toString() { return "Cliente[id=" + id + ", nombre=" + nombre + ", email=" + email + ", activo=" + activo + "]"; }
}
