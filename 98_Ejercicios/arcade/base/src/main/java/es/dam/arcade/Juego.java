package es.dam.arcade;

import jakarta.persistence.*;
import lombok.Getter;

public class Juego {
    @Getter
    private String id;
    private String titulo;
    private String genero;
    private int precio;
}
