package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "direcciones")
public class Direccion {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private String calle;
 private String ciudad;
 private String codigoPostal;
 protected Direccion() {}
 public Direccion(String calle, String ciudad, String codigoPostal) {
  this.calle=calle;
  this.ciudad=ciudad;
  this.codigoPostal=codigoPostal;
 }
 public Long getId() { return id; }
 public String getCalle() { return calle; }
 public String getCiudad() { return ciudad; }
 public String getCodigoPostal() { return codigoPostal; }

}
