package com.ejemplo.config;
import com.ejemplo.modelo.*;
import com.ejemplo.repositorio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Carga determinista: la transacción se abre explícitamente durante PostConstruct. */
@Component("dataSeed")
public class DataSeed {
 private final CategoriaRepository categorias;
 private final ProductoRepository productos;
 private final ClienteRepository clientes;
 private final PedidoRepository pedidos;
 private final TransactionTemplate tx;
 public DataSeed(CategoriaRepository categorias, ProductoRepository productos,
                 ClienteRepository clientes, PedidoRepository pedidos, TransactionTemplate tx) {
  this.categorias=categorias; this.productos=productos; this.clientes=clientes; this.pedidos=pedidos; this.tx=tx;
 }
 @PostConstruct
 public void cargar() {
  tx.executeWithoutResult(status -> {
   if (categorias.count()!=0 || clientes.count()!=0) return;
   Categoria informatica=categorias.save(new Categoria("Informática"));
   Categoria hogar=categorias.save(new Categoria("Hogar"));
   categorias.save(new Categoria("Sin productos"));
   productos.saveAll(List.of(
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
   pedido(ana,"2026-01-10T10:00:00","1280.00","PENDIENTE");
   pedido(ana,"2026-01-15T12:00:00","50.00","ENVIADO");
   pedido(bruno,"2026-01-20T09:00:00","250.00","EN_PROCESO");
   pedido(bruno,"2026-01-31T23:59:59","500.00","ENVIADO");
   pedido(carla,"2026-01-01T00:00:00","100.00","CANCELADO");
   pedido(carla,"2025-12-31T23:59:59","700.00","ENVIADO");
   pedido(ana,"2026-02-01T00:00:00","90.00","PENDIENTE");
   pedido(bruno,"2026-01-01T00:00:00","300.00","PENDIENTE");
  });
 }
 private void pedido(Cliente c,String fecha,String total,String estado) {
  pedidos.save(new Pedido(LocalDateTime.parse(fecha),new BigDecimal(total),estado,c));
 }
}
