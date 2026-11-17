package com.ejemplo.controlador;

import com.ejemplo.dto.ProductoDTO;
import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.servicio.ProductoService;
import com.ejemplo.servicio.ProductoFilterParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@Validated
@RequestMapping("/api/v1/productos")
public class ProductoController {
 private final ProductoService productoService;
 private final ProductoFilterParser parser;
 public ProductoController(ProductoService productoService, ProductoFilterParser parser) {
  this.productoService = productoService;
  this.parser = parser;
 }
 @GetMapping("/search-a")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaA(
   @RequestParam(required = false) @Size(min = 2, max = 50) String nombre,
   @RequestParam(required = false) @DecimalMin("0.0") BigDecimal precioMin,
   @RequestParam(required = false) @DecimalMax("10000.0") BigDecimal precioMax,
   @RequestParam(required = false) String nombreCategoria,
   @PageableDefault(size = 10) Pageable pageable) {
  ProductoFilterDTO filtro = new ProductoFilterDTO();
  filtro.setNombre(nombre);
  filtro.setPrecioMin(precioMin);
  filtro.setPrecioMax(precioMax);
  filtro.setNombreCategoria(nombreCategoria);
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }
 @GetMapping("/search-b")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaB(
   @RequestParam MultiValueMap<String, String> queryParams,
   @PageableDefault(size = 10) Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(parser.parsear(queryParams), pageable));
 }
 @PostMapping("/search")
 public ResponseEntity<Page<ProductoDTO>> filtrarProductos(
   @Valid @RequestBody ProductoFilterDTO filtro,
   @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }
}
