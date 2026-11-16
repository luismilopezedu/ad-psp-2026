package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/*
  Obtener los clientes con pedidos PENDIENTES superiores a X €.
  Debe aparecer los clientes, su direccion, los pedidos,
  las líneas de pedido, los productos y su categoría.

  Se puede usar JPQL o derivadas, y hay que usar grafos de entidad con subgrafos.

 */


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
 @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
 @JoinColumn(name = "direccion_id")
 private Direccion direccion;
 @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
 private java.util.List<Pedido> pedidos = new java.util.ArrayList<>();
 public Direccion getDireccion() { return direccion; }
 public void setDireccion(Direccion direccion) { this.direccion=direccion; }
 public java.util.List<Pedido> getPedidos() { return pedidos; }
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
