package com.ejemplo;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.hibernate.Hibernate;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

// Sin @Transactional: las asociaciones se leen después de cerrar la sesión.
@SpringBootTest(properties={"app.demo.enabled=false","spring.jpa.show-sql=false",
 "spring.datasource.url=jdbc:h2:mem:test-grafos;DB_CLOSE_DELAY=-1"})
class GrafosEntidadTest {
 @Autowired ClienteRepository clientes;
 @Autowired PedidoRepository pedidos;
 private Long idAna() {
  return clientes.findByEmailAndActivoTrue("ana@ejemplo.com").orElseThrow().getId();
 }
 private Long idPedido() {
  return pedidos.findCompletoByClienteId(idAna()).stream()
    .filter(p -> p.getTotal().compareTo(new BigDecimal("1280"))==0)
    .findFirst().orElseThrow().getId();
 }
 @Test void direccionYRelacionOpcional() {
  var ana=clientes.findWithDireccionByEmail("ana@ejemplo.com").orElseThrow();
  assertThat(ana.getDireccion().getCiudad()).isEqualTo("Sevilla");
  assertThat(Hibernate.isInitialized(ana.getPedidos())).isFalse();
  assertThat(clientes.findWithDireccionByEmail("daniel@ejemplo.com").orElseThrow().getDireccion()).isNull();
  assertThat(clientes.findWithDireccionByEmail("nadie@ejemplo.com")).isEmpty();
 }
 @Test void rutasCompuestas() {
  var lista=pedidos.findByEstado("PENDIENTE");
  assertThat(lista).hasSize(3);
  lista.forEach(p -> {
   assertThat(p.getCliente().getDireccion().getCiudad()).isNotBlank();
   assertThat(p.getLineas()).isNotEmpty();
   p.getLineas().forEach(l -> assertThat(l.getProducto().getCategoria().getNombre()).isNotBlank());
  });
  assertThat(pedidos.findByEstado("INEXISTENTE")).isEmpty();
 }
 @Test void metodoEstandar() {
  var p=pedidos.findById(idPedido()).orElseThrow();
  assertThat(p.getCliente().getNombre()).isEqualTo("Ana López");
  assertThat(p.getLineas()).hasSize(2);
  p.getLineas().forEach(l -> assertThat(l.getProducto().getNombre()).isNotBlank());
  assertThat(pedidos.findById(-1L)).isEmpty();
 }
 @Test void queryConEtiquetasYUmbralEstricto() {
  var lista=pedidos.buscarPedidosGrandesConEtiquetas(new BigDecimal("600"));
  assertThat(lista).hasSize(2);
  lista.forEach(p -> p.getLineas().forEach(l ->
    assertThat(Hibernate.isInitialized(l.getProducto().getEtiquetas())).isTrue()));
  assertThat(pedidos.buscarPedidosGrandesConEtiquetas(new BigDecimal("1280"))).isEmpty();
 }
 @Test void grafoNombradoFetchYLoad() {
  var fetch=pedidos.findCompletoById(idPedido()).orElseThrow();
  comprobarCompleto(fetch);
  var load=pedidos.findCompletoLoadById(fetch.getId()).orElseThrow();
  comprobarCompleto(load);
  assertThat(load.getLineas()).hasSize(2);
  assertThat(fetch.getLineas()).hasSize(2); // Un producto tiene dos etiquetas: no duplicar líneas.
  assertThat(fetch.getLineas().stream().filter(l -> l.getProducto().getNombre().equals("Portátil Pro"))
    .findFirst().orElseThrow().getProducto().getEtiquetas()).hasSize(2);
  assertThat(pedidos.findCompletoByClienteId(idAna())).hasSize(3).allSatisfy(this::comprobarCompleto);
  assertThat(pedidos.findCompletoById(-1L)).isEmpty();
 }
 @Test void totalesCoincidenConLasLineas() {
  pedidos.findAll().forEach(p -> {
   var completo=pedidos.findCompletoById(p.getId()).orElseThrow();
   var suma=completo.getLineas().stream()
    .map(l -> l.getPrecioUnitario().multiply(BigDecimal.valueOf(l.getCantidad())))
    .reduce(BigDecimal.ZERO,BigDecimal::add);
   assertThat(suma).isEqualByComparingTo(completo.getTotal());
  });
 }
 private void comprobarCompleto(Pedido p) {
  assertThat(p.getCliente().getDireccion().getCalle()).isNotBlank();
  assertThat(p.getLineas()).isNotEmpty();
  p.getLineas().forEach(l -> {
   assertThat(l.getProducto().getCategoria().getNombre()).isNotBlank();
   assertThat(Hibernate.isInitialized(l.getProducto().getEtiquetas())).isTrue();
   l.getProducto().getEtiquetas().forEach(e -> assertThat(e.getNombre()).isNotBlank());
  });
 }
}
