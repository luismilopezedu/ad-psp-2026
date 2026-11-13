package com.ejemplo.repositorio;
import com.ejemplo.modelo.*;
import com.ejemplo.dto.ResumenPedidoDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
 List<Producto> findByCategoria_Nombre(String nombreCategoria);

 @Query("SELECT p FROM Producto p JOIN p.categoria c WHERE c.nombre = :nombreCategoria")
    List<Producto> buscarPorNombreCategoria(@Param("nombreCategoria") String nombreCategoria);

 @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.precio >= :precioMinimo")
    List<Producto> buscarProductosConCategoriaCargada(@Param("precioMinimo") BigDecimal precioMinimo);

 @Query("SELECT p FROM Producto p " +
           "WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "AND COALESCE(p.precio, 0) > :precioMinimo")
    List<Producto> buscarPorFiltroTextoYPrecio(
            @Param("filtro") String filtro,
            @Param("precioMinimo") BigDecimal precioMinimo
    );

 @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId")
    Page<Producto> buscarPorCategoriaPaginado(
            @Param("categoriaId") Long categoriaId, 
            Pageable pageable
    );

 @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorNombreContiene(@Param("termino") String termino);

 @Query("SELECT p FROM Producto p WHERE COALESCE(p.precio, 0) <= :precioMaximo")
    List<Producto> buscarPorPrecioMaximoSeguro(@Param("precioMaximo") BigDecimal precioMaximo);
 // Compatibilidad con los ejemplos Double del tutorial; usar BigDecimal en código nuevo.
 default List<Producto> buscarProductosConCategoriaCargada(Double minimo) {
  return buscarProductosConCategoriaCargada(BigDecimal.valueOf(Objects.requireNonNull(minimo)));
 }

 // Compatibilidad con los ejemplos Double del tutorial; usar BigDecimal en código nuevo.
 default List<Producto> buscarPorFiltroTextoYPrecio(String filtro, Double minimo) {
  return buscarPorFiltroTextoYPrecio(filtro, BigDecimal.valueOf(Objects.requireNonNull(minimo)));
 }

}
