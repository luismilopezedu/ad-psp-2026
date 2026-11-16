package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lineas_pedido")
public class LineaPedido {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private Integer cantidad;
 @Column(precision = 12, scale = 2)
 private BigDecimal precioUnitario;
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "pedido_id", nullable = false)
 private Pedido pedido;
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "producto_id", nullable = false)
 private Producto producto;
 protected LineaPedido() {}
 public LineaPedido(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
  this.producto=producto;
  this.cantidad=cantidad;
  this.precioUnitario=precioUnitario;
 }
 public Long getId() { return id; }
 public Integer getCantidad() { return cantidad; }
 public BigDecimal getPrecioUnitario() { return precioUnitario; }
 public Pedido getPedido() { return pedido; }
 public Producto getProducto() { return producto; }
 public void setPedido(Pedido pedido) { this.pedido=pedido; }
}
