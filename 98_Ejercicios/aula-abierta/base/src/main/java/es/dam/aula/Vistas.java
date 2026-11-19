package es.dam.aula;

import java.util.List;

public final class Vistas {
    private Vistas() {}

    public record Alta(String id, String titulo, String nivel, int horas, String areaId) {}
    public record Detalle(String id, String areaId, List<String> lecciones) {}
    public record Programa(String id, List<Detalle> cursos) {}
    public record Filtro(List<String> nivel, Integer minHoras) {}
    public record Pagina(List<String> ids, int pagina, int totalPaginas, long totalElementos) {}
}
