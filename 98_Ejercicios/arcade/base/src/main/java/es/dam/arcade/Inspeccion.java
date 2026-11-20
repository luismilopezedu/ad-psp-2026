package es.dam.arcade;

import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class Inspeccion {
    private final JdbcClient jdbc;
    public Inspeccion(JdbcClient jdbc) { this.jdbc = jdbc; }

    public Map<String,Object> comprobarJ1() {
        var retirado = jdbc.sql("select retirado from juego where id='J1'").query(Boolean.class).single();
        var logros = jdbc.sql("select count(*) from logro where id in ('L1','L2')").query(Integer.class).single();
        var pc = jdbc.sql("select count(*) from plataforma where id='PC'").query(Integer.class).single();
        return Map.of("retirado", retirado, "logrosOriginales", logros, "plataformaExiste", pc == 1);
    }
}
