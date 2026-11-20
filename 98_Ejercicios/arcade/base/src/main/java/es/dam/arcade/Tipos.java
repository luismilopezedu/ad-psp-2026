package es.dam.arcade;

import java.util.List;
import org.springframework.data.domain.Page;

public class Tipos {
    public record Alta(String id, String titulo, String genero, int precio, String plataforma) {}
    public record Filtro(List<String> generos, Integer minimo) {}
    public record Detalle(String id, List<String> logros) {}
    public record Programa(String id, List<Detalle> juegos) {}
    public record Pagina(List<String> ids, long total, int paginas) {
        public static Pagina de(Page<Juego> datos) {
            return new Pagina(datos.map(Juego::getId).getContent(),
                    datos.getTotalElements(), datos.getTotalPages());
        }
    }
}
