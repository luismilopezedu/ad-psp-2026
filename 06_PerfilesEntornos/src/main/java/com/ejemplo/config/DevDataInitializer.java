package com.ejemplo.config;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Datos ficticios solo en desarrollo; antes de ejecutar las demostraciones. */
@Component
@Profile("dev & !prod")
@Order(1)
public class DevDataInitializer implements CommandLineRunner {
 private final CategoriaRepository categorias;
 private final ProductoRepository productos;
 private final ClienteRepository clientes;
 private final PedidoRepository pedidos;
 private final EtiquetaRepository etiquetas;
 private final TransactionTemplate tx;
 public DevDataInitializer(CategoriaRepository categorias, ProductoRepository productos,
                 ClienteRepository clientes, PedidoRepository pedidos, EtiquetaRepository etiquetas, TransactionTemplate tx) {
  this.categorias=categorias; this.productos=productos; this.clientes=clientes; this.pedidos=pedidos; this.tx=tx; this.etiquetas=etiquetas;
 }
 @Override
 public void run(String... args) {
  System.out.println(">>> [DEV] Cargando datos de ejemplo en PostgreSQL...");
  tx.executeWithoutResult(status -> {
   if (categorias.count()!=0 || clientes.count()!=0) return;
   Categoria informatica=categorias.save(new Categoria("Informática"));
   Categoria hogar=categorias.save(new Categoria("Hogar"));
   categorias.save(new Categoria("Sin productos"));
   var catalogo=productos.saveAll(List.of(
    new Producto("Portátil Pro", new BigDecimal("1200.00"), informatica),
    new Producto("Teclado mecánico", new BigDecimal("80.00"), informatica),
    new Producto("Ratón inalámbrico", new BigDecimal("25.00"), informatica),
    new Producto("Monitor 27", new BigDecimal("250.00"), informatica),
    new Producto("Auriculares", new BigDecimal("60.00"), informatica),
    new Producto("Webcam", new BigDecimal("45.00"), informatica),
    new Producto("Cable USB", new BigDecimal("10.00"), informatica),
    new Producto("Memoria USB", new BigDecimal("15.00"), informatica),
    new Producto("Disco SSD", new BigDecimal("100.00"), informatica),
    new Producto("Router", new BigDecimal("90.00"), informatica),
    new Producto("Impresora", new BigDecimal("180.00"), informatica),
    new Producto("Altavoces", new BigDecimal("35.00"), informatica),
    new Producto("Lámpara", new BigDecimal("30.00"), hogar),
    new Producto("Mesa", new BigDecimal("150.00"), hogar),
    new Producto("Producto sin precio", null, hogar),
    new Producto("Muestra gratuita", BigDecimal.ZERO, hogar)));
   Cliente ana=clientes.save(new Cliente("Ana López", "ana@ejemplo.com", true));
   Cliente bruno=clientes.save(new Cliente("Bruno Díaz", "bruno@ejemplo.com", true));
   Cliente carla=clientes.save(new Cliente("Carla Ruiz", "carla@ejemplo.com", false));
   clientes.save(new Cliente("Daniel Gil", "daniel@ejemplo.com", true));
   ana.setDireccion(new Direccion("Calle Sol 10", "Sevilla", "41001"));
   bruno.setDireccion(new Direccion("Calle Luna 20", "Mairena del Aljarafe", "41927"));
   carla.setDireccion(new Direccion("Calle Río 3", "Sevilla", "41002"));
   clientes.saveAll(List.of(ana,bruno,carla));
   var oferta=etiquetas.save(new Etiqueta("Oferta"));
   var destacado=etiquetas.save(new Etiqueta("Destacado"));
   var sostenible=etiquetas.save(new Etiqueta("Sostenible"));
   catalogo.get(0).getEtiquetas().addAll(List.of(oferta,destacado));
   catalogo.get(1).getEtiquetas().add(oferta);
   catalogo.get(3).getEtiquetas().add(destacado);
   catalogo.get(13).getEtiquetas().add(sostenible);
   pedido(ana,"2026-01-10T10:00:00","1280.00","PENDIENTE",linea(catalogo.get(0),1),linea(catalogo.get(1),1));
   pedido(ana,"2026-01-15T12:00:00","50.00","ENVIADO",linea(catalogo.get(2),2));
   pedido(bruno,"2026-01-20T09:00:00","250.00","EN_PROCESO",linea(catalogo.get(3),1));
   pedido(bruno,"2026-01-31T23:59:59","500.00","ENVIADO",linea(catalogo.get(3),2));
   pedido(carla,"2026-01-01T00:00:00","100.00","CANCELADO",linea(catalogo.get(8),1));
   pedido(carla,"2025-12-31T23:59:59","700.00","ENVIADO",linea(catalogo.get(3),2),linea(catalogo.get(8),2));
   pedido(ana,"2026-02-01T00:00:00","90.00","PENDIENTE",linea(catalogo.get(9),1));
   pedido(bruno,"2026-01-01T00:00:00","300.00","PENDIENTE",linea(catalogo.get(13),2));
  });
 }
 private LineaPedido linea(Producto producto, int cantidad) {
  return new LineaPedido(producto,cantidad,producto.getPrecio());
 }
 private void pedido(Cliente c,String fecha,String total,String estado, LineaPedido... lineas) {
  Pedido pedido=new Pedido(LocalDateTime.parse(fecha),new BigDecimal(total),estado,c);
  for (LineaPedido linea : lineas) pedido.addLinea(linea);
  c.getPedidos().add(pedido);
  pedidos.save(pedido);
 }
}
