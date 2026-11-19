package es.dam.aula;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Profile({"default","local"})
class Diagnostico {
    private final JdbcClient jdbc;

    Diagnostico(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Sonda didáctica para la prueba de retirada de C1 del juego inicial.
    @GetMapping("/diagnostico/c1")
    Map<String, Object> comprobarBaja() {
        boolean retirado = jdbc.sql("select retirado from curso where id='C1'")
                .query(Boolean.class).single();
        int lecciones = jdbc.sql("select count(*) from leccion where id in ('L1','L2')")
                .query(Integer.class).single();
        int areas = jdbc.sql("select count(*) from area where id='A'")
                .query(Integer.class).single();
        return Map.of("retirado", retirado, "lecciones", lecciones, "areaExiste", areas == 1);
    }
}
