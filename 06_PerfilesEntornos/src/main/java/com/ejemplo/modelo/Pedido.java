package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@NamedEntityGraph(
    name = "Pedido.completoConDetalles",
    attributeNodes = {
        // Atributo simple con subgrafo para llegar a la dirección del cliente
        @NamedAttributeNode(value = "cliente", subgraph = "subgrafo-cliente-direccion"),
        // Atributo de colección con subgrafo para navegar en profundidad por las líneas
        @NamedAttributeNode(value = "lineas", subgraph = "subgrafo-lineas-producto")
    },
    subgraphs = {
        // Subgrafo 1: Cliente -> Direccion
        @NamedSubgraph(
            name = "subgrafo-cliente-direccion",
            attributeNodes = {
                @NamedAttributeNode("direccion")
            }
        ),
        // Subgrafo 2: LineaPedido -> Producto
        @NamedSubgraph(
            name = "subgrafo-lineas-producto",
            attributeNodes = {
                @NamedAttributeNode(value = "producto", subgraph = "subgrafo-producto-detalles")
            }
        ),
        // Subgrafo 3: Producto -> Categoria y Etiquetas
        @NamedSubgraph(
            name = "subgrafo-producto-detalles",
            attributeNodes = {
                @NamedAttributeNode("categoria"),
                @NamedAttributeNode("etiquetas")
            }
        )
    }
)
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
 @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 private java.util.List<LineaPedido> lineas = new java.util.ArrayList<>();
 public java.util.List<LineaPedido> getLineas() { return lineas; }
 public void addLinea(LineaPedido linea) {
  lineas.add(linea);
  linea.setPedido(this);
 }
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
