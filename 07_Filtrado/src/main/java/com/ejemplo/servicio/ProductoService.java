package com.ejemplo.servicio;

import com.ejemplo.dto.ProductoDTO;
import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.especificacion.ProductoSpecifications;
import com.ejemplo.modelo.Producto;
import com.ejemplo.repositorio.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarProductosFiltro(ProductoFilterDTO filtro, Pageable pageable) {
        // 1. Construir la especificacion de predicados
        PredicateSpecification<Producto> spec = ProductoSpecifications.conFiltroDinamico(filtro);

        // 2. Ejecutar la consulta paginada en el repositorio
        Page<Producto> paginaEntidades = productoRepository.findBy(spec, query -> query.page(pageable));

        // 3. Mapear los resultados a DTO de respuesta
        // Hacemos esta transformación aquí para evitar
        // problemas de fetching.
        return paginaEntidades.map(this::convertirADto);
    }

    private ProductoDTO convertirADto(Producto p) {
        String nombreCat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getPrecio(), p.getActivo(), nombreCat);
    }
}
