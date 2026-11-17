package com.ejemplo.repositorio;
import com.ejemplo.modelo.*;
import com.ejemplo.dto.ResumenPedidoDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
 Optional<Cliente> findByEmailAndActivoTrue(String email);

 @Query("SELECT c FROM Cliente c WHERE c.email = :email AND c.activo = true")
    Optional<Cliente> buscarPorEmailActivo(@Param("email") String email);

 @Query("SELECT c FROM Cliente c WHERE c.nombre LIKE %?1% AND c.activo = true")
    List<Cliente> buscarActivosPorNombreParcial(String parteNombre);

 @Query("SELECT c FROM Cliente c WHERE c.email = :email AND c.activo = true")
    Optional<Cliente> buscarClienteActivoPorEmail(@Param("email") String email);

 @Query("SELECT c FROM Cliente c WHERE EXISTS (" +
           "  SELECT p FROM Pedido p WHERE p.cliente = c AND p.total > :montoMinimo" +
           ")")
    List<Cliente> buscarClientesConPedidosMayoresA(@Param("montoMinimo") BigDecimal montoMinimo);
}
