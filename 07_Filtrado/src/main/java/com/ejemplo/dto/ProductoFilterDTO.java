package com.ejemplo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductoFilterDTO {

    @Size(min = 2, max = 50, message = "El nombre de búsqueda debe tener entre 2 y 50 caracteres")
    private String nombre;

    @DecimalMin(value = "0.0", message = "El precio mínimo no puede ser negativo")
    private BigDecimal precioMin;

    @DecimalMax(value = "10000.0", message = "El precio máximo superado")
    private BigDecimal precioMax;

    private String nombreCategoria;

    private Boolean activo;

    // Getters y Setters...
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioMin() { return precioMin; }
    public void setPrecioMin(BigDecimal precioMin) { this.precioMin = precioMin; }
    public BigDecimal getPrecioMax() { return precioMax; }
    public void setPrecioMax(BigDecimal precioMax) { this.precioMax = precioMax; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
