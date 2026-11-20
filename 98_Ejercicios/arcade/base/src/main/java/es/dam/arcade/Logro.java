package es.dam.arcade;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Logro {
    @Id
    private String id;
    private String nombre;
}
