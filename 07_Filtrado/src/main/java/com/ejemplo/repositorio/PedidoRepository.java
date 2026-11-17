package com.ejemplo.repositorio;
import com.ejemplo.modelo.*;
import com.ejemplo.dto.ResumenPedidoDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
 @Query("SELECT p FROM Pedido p WHERE p.fecha BETWEEN :inicio AND :fin AND p.estado IN :estados")
    List<Pedido> buscarPorRangoFechasYEstados(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estados") Collection<String> estados
    );

 @Query("SELECT new com.ejemplo.dto.ResumenPedidoDTO(p.id, p.cliente.nombre, p.total) " +
           "FROM Pedido p " +
           "WHERE p.estado = :estado")
    List<ResumenPedidoDTO> obtenerResumenesPorEstado(@Param("estado") String estado);

 @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.estado = :estado")
    List<Pedido> buscarPedidosConClienteCargado(@Param("estado") String estado);

 @Query("SELECT p FROM Pedido p WHERE p.estado IN :estados")
    List<Pedido> buscarPorEstados(@Param("estados") Collection<String> estados);

 @Query("SELECT p FROM Pedido p WHERE p.fecha BETWEEN :desde AND :hasta AND p.total >= :totalMinimo")
    List<Pedido> buscarPedidosEnRangoYTotal(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("totalMinimo") BigDecimal totalMinimo
    );

 @Query("SELECT new com.ejemplo.dto.ResumenPedidoDTO(p.id, p.cliente.nombre, p.total, p.fecha) " +
           "FROM Pedido p " +
           "WHERE p.estado = :estado")
    List<ResumenPedidoDTO> obtenerResumenesConFechaPorEstado(@Param("estado") String estado);
}
