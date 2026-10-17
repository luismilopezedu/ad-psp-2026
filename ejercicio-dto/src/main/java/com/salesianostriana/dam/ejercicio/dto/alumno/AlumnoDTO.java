package com.salesianostriana.dam.ejercicio.dto.alumno;

import org.springframework.util.StringUtils;

public record AlumnoDTO(
        String nombre,
        String apellidos,
        String email,
        String curso,
        String direccion
) {


    public static AlumnoDTO of(Alumno alumno) {

        if (alumno == null)
            return null;

        return new AlumnoDTO(
                alumno.getNombre(),
                alumno.getApellido1() + " " + alumno.getApellido2(),
                alumno.getEmail(),
                alumno.getCurso() != null ? alumno.getCurso().getNombre() : null,
                formatearDireccion(alumno.getDireccion())
        );
    }

    private static String formatearDireccion(Direccion direccion) {
        /*return "%s %s. CP %s. %s (%s)".formatted(
                direccion.getTipoVia(),
                direccion.getLinea1() +
                        (StringUtils.hasText(direccion.getLinea2()) ? " " + direccion.getLinea2() : ""),
                direccion.getCp(),
                direccion.getPoblacion(),
                direccion.getProvincia()
        );*/




        /*return direccion.getTipoVia() + " " +                 direccion.getLinea1() +
                (StringUtils.hasText(direccion.getLinea2()) ? " " + direccion.getLinea2() : "") +
                ". CP " +  direccion.getCp() + ". " + direccion.getPoblacion() +
                " (" + direccion.getProvincia() + ")";
*/
        return String.format("%s %s. CP %s. %s (%s)",
                direccion.getTipoVia(),
                direccion.getLinea1() +
                        (StringUtils.hasText(direccion.getLinea2()) ? " " + direccion.getLinea2() : ""),
                direccion.getCp(),
                direccion.getPoblacion(),
                direccion.getProvincia()
                );




    }
}
