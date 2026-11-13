package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(nullable = false)
 private LocalDateTime fecha;
 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal total;
 @Column(nullable = false)
 private String estado;
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "cliente_id", nullable = false)
 private Cliente cliente;
 protected Pedido() {}
 public Pedido(LocalDateTime fecha, BigDecimal total, String estado, Cliente cliente) {
  this.fecha = fecha;
  this.total = total;
  this.estado = estado;
  this.cliente = cliente;
 }
 public Long getId() { return id; }
 public LocalDateTime getFecha() { return fecha; }
 public BigDecimal getTotal() { return total; }
 public String getEstado() { return estado; }
 public Cliente getCliente() { return cliente; }
 @Override public String toString() { return "Pedido[id=" + id + ", fecha=" + fecha + ", total=" + total + ", estado=" + estado + "]"; }
}
