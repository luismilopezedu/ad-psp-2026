package com.ejemplo;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.hibernate.Hibernate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

// Intencionadamente sin @Transactional: comprueba FETCH con entidades desconectadas.
@SpringBootTest(properties={"app.demo.enabled=false","spring.jpa.show-sql=false",
 "spring.datasource.url=jdbc:h2:mem:test-jpql;DB_CLOSE_DELAY=-1"})
class ConsultasJpqlTest {
 @Autowired ClienteRepository clientes;
 @Autowired ProductoRepository productos;
 @Autowired PedidoRepository pedidos;
 @Autowired CategoriaRepository categorias;
 private final LocalDateTime inicio=LocalDateTime.parse("2026-01-01T00:00:00");
 private final LocalDateTime fin=LocalDateTime.parse("2026-01-31T23:59:59");
 @Test void clientesYParametros() {
  assertThat(clientes.count()).isEqualTo(4);
  var id=clientes.findByEmailAndActivoTrue("ana@ejemplo.com").orElseThrow().getId();
  assertThat(clientes.buscarPorEmailActivo("ana@ejemplo.com").orElseThrow().getId()).isEqualTo(id);
  assertThat(clientes.buscarClienteActivoPorEmail("ana@ejemplo.com").orElseThrow().getId()).isEqualTo(id);
  assertThat(clientes.buscarPorEmailActivo("carla@ejemplo.com")).isEmpty();
  assertThat(clientes.buscarPorEmailActivo("nadie@ejemplo.com")).isEmpty();
  assertThat(clientes.buscarActivosPorNombreParcial("Ana")).extracting(Cliente::getEmail).containsExactly("ana@ejemplo.com");
 }
 @Test void filtrosDeProductos() {
  assertThat(productos.count()).isEqualTo(16);
  assertThat(productos.findByCategoria_Nombre("Informática")).hasSize(12);
  assertThat(productos.buscarPorNombreCategoria("Informática")).hasSize(12);
  assertThat(productos.buscarPorNombreCategoria("Sin productos")).isEmpty();
  assertThat(productos.buscarPorNombreContiene("MONITOR")).extracting(Producto::getNombre).containsExactly("Monitor 27");
  assertThat(productos.buscarPorFiltroTextoYPrecio("TECLADO",new BigDecimal("50"))).hasSize(1);
  assertThat(productos.buscarPorFiltroTextoYPrecio("TECLADO",50.0)).hasSize(1);
  assertThat(productos.buscarPorFiltroTextoYPrecio("TECLADO",80.0)).isEmpty();
  assertThat(productos.buscarPorPrecioMaximoSeguro(BigDecimal.ZERO)).extracting(Producto::getNombre)
   .containsExactlyInAnyOrder("Producto sin precio","Muestra gratuita");
 }
 @Test void fetchFueraDeTransaccion() {
  var simples=productos.buscarPorNombreCategoria("Informática");
  assertThat(Hibernate.isInitialized(simples.getFirst().getCategoria())).isFalse();
  var lista=productos.buscarProductosConCategoriaCargada(new BigDecimal("100"));
  assertThat(lista).hasSize(5);
  assertThat(productos.buscarProductosConCategoriaCargada(100.0)).hasSize(5);
  lista.forEach(p -> { assertThat(Hibernate.isInitialized(p.getCategoria())).isTrue(); assertThat(p.getCategoria().getNombre()).isNotBlank(); });
  var listaPedidos=pedidos.buscarPedidosConClienteCargado("PENDIENTE");
  assertThat(listaPedidos).hasSize(3);
  listaPedidos.forEach(p -> { assertThat(Hibernate.isInitialized(p.getCliente())).isTrue(); assertThat(p.getCliente().getNombre()).isNotBlank(); });
 }
 @Test void fechasEstadosYExtremosInclusivos() {
  assertThat(pedidos.count()).isEqualTo(8);
  assertThat(pedidos.buscarPorEstados(List.of("PENDIENTE","EN_PROCESO"))).hasSize(4);
  assertThat(pedidos.buscarPorEstados(List.of("INEXISTENTE"))).isEmpty();
  assertThat(pedidos.buscarPorRangoFechasYEstados(inicio,fin,List.of("PENDIENTE","EN_PROCESO"))).hasSize(3);
  var rango=pedidos.buscarPedidosEnRangoYTotal(inicio,fin,new BigDecimal("300"));
  assertThat(rango).hasSize(3);
  assertThat(rango).extracting(Pedido::getFecha).contains(inicio,fin);
 }
 @Test void subconsultaYProyecciones() {
  assertThat(clientes.buscarClientesConPedidosMayoresA(new BigDecimal("600"))).extracting(Cliente::getNombre).containsExactlyInAnyOrder("Ana López","Carla Ruiz");
  assertThat(clientes.buscarClientesConPedidosMayoresA(new BigDecimal("1280"))).isEmpty();
  var resumen=pedidos.obtenerResumenesPorEstado("ENVIADO");
  assertThat(resumen).hasSize(3);
  assertThat(resumen).allSatisfy(d -> { assertThat(d.getFecha()).isNull(); assertThat(d.getNombreCliente()).isNotBlank(); assertThat(d.getTotal()).isPositive(); });
  var fechado=pedidos.obtenerResumenesConFechaPorEstado("ENVIADO");
  assertThat(fechado).hasSize(3);
  assertThat(fechado).allSatisfy(d -> assertThat(d.getFecha()).isNotNull());
  assertThat(fechado).extracting(d -> d.getPedidoId()).containsExactlyInAnyOrderElementsOf(resumen.stream().map(d -> d.getPedidoId()).toList());
 }
 @Test void paginacionYOrden() {
  var id=categorias.findByNombre("Informática").orElseThrow().getId();
  var primera=productos.buscarPorCategoriaPaginado(id,PageRequest.of(0,10,Sort.by("precio")));
  var segunda=productos.buscarPorCategoriaPaginado(id,PageRequest.of(1,10,Sort.by("precio")));
  assertThat(primera.getTotalElements()).isEqualTo(12);
  assertThat(primera.getTotalPages()).isEqualTo(2);
  assertThat(primera.getContent()).hasSize(10);
  assertThat(segunda.getContent()).extracting(Producto::getNombre).containsExactly("Monitor 27","Portátil Pro");
  assertThat(primera.getContent()).extracting(Producto::getPrecio).isSorted();
  assertThat(productos.buscarPorCategoriaPaginado(id,PageRequest.of(0,10,Sort.by("nombre"))).getContent()).extracting(Producto::getNombre).isSorted();
 }
}
