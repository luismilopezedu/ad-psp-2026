package com.ejemplo.servicio;

import com.ejemplo.dto.ProductoFilterDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import java.math.BigDecimal;

/** Alternativa B: conversión y validación manual, tomando el primer valor de cada filtro. */
@Component
public class ProductoFilterParser {
 public ProductoFilterDTO parsear(MultiValueMap<String, String> params) {
  ProductoFilterDTO filtro = new ProductoFilterDTO();
  String nombre = params.getFirst("nombre");
  if (nombre != null && (nombre.length() < 2 || nombre.length() > 50)) {
   throw new IllegalArgumentException("El nombre de búsqueda debe tener entre 2 y 50 caracteres");
  }
  filtro.setNombre(nombre);
  filtro.setPrecioMin(decimal(params.getFirst("precioMin"), "precioMin"));
  filtro.setPrecioMax(decimal(params.getFirst("precioMax"), "precioMax"));
  if (filtro.getPrecioMin() != null && filtro.getPrecioMin().signum() < 0) {
   throw new IllegalArgumentException("El precio mínimo no puede ser negativo");
  }
  if (filtro.getPrecioMax() != null && filtro.getPrecioMax().compareTo(new BigDecimal("10000.0")) > 0) {
   throw new IllegalArgumentException("El precio máximo superado");
  }
  filtro.setNombreCategoria(params.getFirst("nombreCategoria"));
  String activo = params.getFirst("activo");
  if (activo != null) {
   if (!activo.equalsIgnoreCase("true") && !activo.equalsIgnoreCase("false")) {
    throw new IllegalArgumentException("activo debe ser true o false");
   }
   filtro.setActivo(Boolean.valueOf(activo));
  }
  return filtro;
 }
 private BigDecimal decimal(String valor, String campo) {
  if (valor == null) return null;
  try {
   return new BigDecimal(valor);
  } catch (NumberFormatException ex) {
   throw new IllegalArgumentException(campo + " debe ser un número decimal válido");
  }
 }
}
