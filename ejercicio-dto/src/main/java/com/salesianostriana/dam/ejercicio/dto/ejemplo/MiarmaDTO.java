package com.salesianostriana.dam.ejercicio.dto.ejemplo;

public record MiarmaDTO(String nombre, int edad) {


    public static MiarmaDTO of(Miarma miarma) {
        return new MiarmaDTO(miarma.getNombre(), miarma.getEdad());
    }

}
