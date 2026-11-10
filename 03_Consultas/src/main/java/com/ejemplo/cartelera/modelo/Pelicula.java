package com.ejemplo.cartelera.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "peliculas")
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 50)
    private String genero;

    @Column(nullable = false)
    private int duracionMinutos;

    @Column(nullable = false)
    private boolean disponible;

    @Column(nullable = false)
    private LocalDate fechaEstreno;

    // Constructor exigido por JPA.
    protected Pelicula() {
    }

    public Pelicula(String titulo, String genero, int duracionMinutos,
                   boolean disponible, LocalDate fechaEstreno) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.disponible = disponible;
        this.fechaEstreno = fechaEstreno;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getGenero() {
        return genero;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public LocalDate getFechaEstreno() {
        return fechaEstreno;
    }

    @Override
    public String toString() {
        return "Pelicula{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", genero='" + genero + '\'' +
                ", duracionMinutos=" + duracionMinutos +
                ", disponible=" + disponible +
                ", fechaEstreno=" + fechaEstreno +
                '}';
    }
}
