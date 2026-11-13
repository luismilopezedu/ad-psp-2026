package com.ejemplo.demo;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Sin transacción global: JOIN FETCH permite leer asociaciones fuera del repositorio. */
@Component
@DependsOn("dataSeed")
@ConditionalOnProperty(name="app.demo.enabled", havingValue="true", matchIfMissing=true)
public class ConsultasDemo {
 private final ClienteRepository clientes;
 private final ProductoRepository productos;
 private final PedidoRepository pedidos;
 private final CategoriaRepository categorias;
 public ConsultasDemo(ClienteRepository clientes, ProductoRepository productos,
                      PedidoRepository pedidos, CategoriaRepository categorias) {
  this.clientes=clientes; this.productos=productos; this.pedidos=pedidos; this.categorias=categorias;
 }
 @PostConstruct
 public void ejecutar() {
  var inicio=LocalDateTime.parse("2026-01-01T00:00:00");
  var fin=LocalDateTime.parse("2026-01-31T23:59:59");
  mostrar("Tutorial 2 / Ej.1: derivada", clientes.findByEmailAndActivoTrue("ana@ejemplo.com"));
  mostrar("Tutorial 2 / Ej.1: JPQL", clientes.buscarPorEmailActivo("ana@ejemplo.com"));
  mostrar("Tutorial 2 / Ej.1: parámetro posicional", clientes.buscarActivosPorNombreParcial("Ana"));
  mostrar("Tutorial 3 / Ej.2: relación derivada", productos.findByCategoria_Nombre("Informática"));
  mostrar("Tutorial 3 / Ej.2 y Bonus 3: JOIN", productos.buscarPorNombreCategoria("Informática"));
  var cargados=productos.buscarProductosConCategoriaCargada(100.0);
  mostrar("Tutorial 3 / Ej.2: JOIN FETCH", cargados);
  cargados.forEach(p -> System.out.println(p.getNombre()+" -> "+p.getCategoria().getNombre()));
  mostrar("Tutorial 4 / Ej.3: BETWEEN e IN", pedidos.buscarPorRangoFechasYEstados(inicio,fin,List.of("PENDIENTE","EN_PROCESO")));
  mostrar("Tutorial 5 / Ej.4: LOWER, CONCAT y COALESCE", productos.buscarPorFiltroTextoYPrecio("TECLADO",50.0));
  Long categoriaId=categorias.findByNombre("Informática").orElseThrow().getId();
  var pagina=productos.buscarPorCategoriaPaginado(categoriaId,PageRequest.of(0,10,Sort.by("precio").ascending().and(Sort.by("id"))));
  mostrar("Tutorial 6 / Ej.5: página 0 por precio",pagina.getContent());
  System.out.println("Total="+pagina.getTotalElements()+", páginas="+pagina.getTotalPages());
  mostrar("Tutorial 7: DTO de tres argumentos",pedidos.obtenerResumenesPorEstado("ENVIADO"));
  mostrar("Bonus 1: cliente activo",clientes.buscarClienteActivoPorEmail("ana@ejemplo.com"));
  mostrar("Bonus 2: búsqueda parcial",productos.buscarPorNombreContiene("MONITOR"));
  var pedidosCargados=pedidos.buscarPedidosConClienteCargado("PENDIENTE");
  mostrar("Bonus 4: pedidos con cliente",pedidosCargados);
  pedidosCargados.forEach(p -> System.out.println(p.getId()+" -> "+p.getCliente().getNombre()));
  mostrar("Bonus 5: IN",pedidos.buscarPorEstados(List.of("PENDIENTE","EN_PROCESO")));
  mostrar("Bonus 6: fechas y total",pedidos.buscarPedidosEnRangoYTotal(inicio,fin,new BigDecimal("300.00")));
  mostrar("Bonus 7: COALESCE (incluye precio nulo)",productos.buscarPorPrecioMaximoSeguro(BigDecimal.ZERO));
  mostrar("Bonus 8: EXISTS",clientes.buscarClientesConPedidosMayoresA(new BigDecimal("600.00")));
  mostrar("Bonus 9: página 0 por nombre",productos.buscarPorCategoriaPaginado(categoriaId,PageRequest.of(0,10,Sort.by("nombre").ascending().and(Sort.by("id")))).getContent());
  mostrar("Bonus 10: DTO de cuatro argumentos",pedidos.obtenerResumenesConFechaPorEstado("ENVIADO"));
  System.out.println("=== FIN DE LA DEMOSTRACIÓN JPQL ===");
 }
 private void mostrar(String titulo,Object resultado) {
  System.out.println("\n=== "+titulo+" ===");
  if (resultado instanceof Iterable<?> elementos) elementos.forEach(System.out::println);
  else System.out.println(resultado);
 }
}
