package com.ejemplo.demo;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/** Cada consulta termina antes de recorrer las asociaciones: no hay sesión abierta aquí. */
@Component
@Profile("dev & !prod")
@Order(2)
@ConditionalOnProperty(name="app.demo.enabled", havingValue="true", matchIfMissing=true)
public class GrafosEntidadDemo implements CommandLineRunner {
 private final ClienteRepository clientes;
 private final PedidoRepository pedidos;
 public GrafosEntidadDemo(ClienteRepository clientes, PedidoRepository pedidos) {
  this.clientes=clientes; this.pedidos=pedidos;
 }
 @Override
 public void run(String... args) {
  System.out.println("\n=== 3.1: Cliente -> Direccion ===");
  var ana=clientes.findWithDireccionByEmail("ana@ejemplo.com").orElseThrow();
  System.out.println(ana.getNombre()+" -> "+ana.getDireccion().getCiudad());

  System.out.println("\n=== 3.2: rutas compuestas ===");
  var pendientes=pedidos.findByEstado("PENDIENTE");
  pendientes.forEach(p -> {
   System.out.println(p+" -> "+p.getCliente().getDireccion().getCiudad());
   p.getLineas().forEach(l -> System.out.println(l.getProducto().getNombre()
     +" -> "+l.getProducto().getCategoria().getNombre()));
  });
  Long id=pendientes.stream().filter(p -> p.getCliente().getId().equals(ana.getId()))
    .filter(p -> p.getTotal().compareTo(new BigDecimal("1000"))>0)
    .findFirst().orElseThrow().getId();

  System.out.println("\n=== 3.2: findById con grafo ===");
  var pedido=pedidos.findById(id).orElseThrow();
  System.out.println(pedido.getCliente().getNombre());
  pedido.getLineas().forEach(l -> System.out.println(l.getProducto().getNombre()));

  System.out.println("\n=== 3.2: @Query y etiquetas ===");
  pedidos.buscarPedidosGrandesConEtiquetas(new BigDecimal("600")).forEach(p -> {
   System.out.println(p);
   p.getLineas().forEach(l -> {
    System.out.println(l.getProducto().getNombre());
    l.getProducto().getEtiquetas().forEach(e -> System.out.println("  "+e.getNombre()));
   });
  });

  System.out.println("\n=== 4: grafo con nombre por id ===");
  mostrarCompleto(pedidos.findCompletoById(id).orElseThrow());
  System.out.println("\n=== 4: grafo con nombre por cliente ===");
  pedidos.findCompletoByClienteId(ana.getId()).forEach(this::mostrarCompleto);
  System.out.println("\n=== 5: mismo grafo en modo LOAD ===");
  mostrarCompleto(pedidos.findCompletoLoadById(id).orElseThrow());
  System.out.println("Todas las asociaciones son LAZY: FETCH y LOAD coinciden en este modelo.");
  System.out.println("=== FIN DE LA DEMOSTRACIÓN DE GRAFOS ===");
 }
 private void mostrarCompleto(Pedido p) {
  System.out.println(p+" -> "+p.getCliente().getNombre()
    +" -> "+p.getCliente().getDireccion().getCiudad());
  p.getLineas().forEach(l -> {
   var producto=l.getProducto();
   System.out.println(l.getCantidad()+" x "+producto.getNombre()
     +" ("+producto.getCategoria().getNombre()+")");
   producto.getEtiquetas().forEach(e -> System.out.println("  "+e.getNombre()));
  });
 }
}
