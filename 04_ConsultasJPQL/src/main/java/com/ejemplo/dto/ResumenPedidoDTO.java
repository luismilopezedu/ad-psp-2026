package com.ejemplo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ResumenPedidoDTO {
 private final Long pedidoId;
 private final String nombreCliente;
 private final BigDecimal total;
 private final LocalDateTime fecha;
 public ResumenPedidoDTO(Long pedidoId, String nombreCliente, BigDecimal total) {
  this(pedidoId, nombreCliente, total, null);
 }
 public ResumenPedidoDTO(Long pedidoId, String nombreCliente, BigDecimal total, LocalDateTime fecha) {
  this.pedidoId=pedidoId; this.nombreCliente=nombreCliente; this.total=total; this.fecha=fecha;
 }
 public Long getPedidoId() { return pedidoId; }
 public String getNombreCliente() { return nombreCliente; }
 public BigDecimal getTotal() { return total; }
 public LocalDateTime getFecha() { return fecha; }
 @Override public String toString() { return "ResumenPedidoDTO[id="+pedidoId+", cliente="+nombreCliente+", total="+total+", fecha="+fecha+"]"; }
}
