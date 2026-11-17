package com.ejemplo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
 "app.demo.enabled=false", "spring.jpa.show-sql=false",
 "spring.datasource.url=jdbc:h2:mem:test-filtrado;DB_CLOSE_DELAY=-1"
})
class FiltradoHttpTest {
 @LocalServerPort int port;
 private final HttpClient cliente = HttpClient.newHttpClient();

 @Test void sinFiltrosYPaginacion() throws Exception {
  var primera = post("", "{}");
  assertThat(primera.statusCode()).isEqualTo(200);
  assertThat(entero(primera, "$.totalElements")).isEqualTo(16);
  assertThat(entero(primera, "$.totalPages")).isEqualTo(2);
  assertThat(entero(primera, "$.content.length()")).isEqualTo(10);
  var segunda = post("?page=1&size=10", "{}");
  assertThat(entero(segunda, "$.content.length()")).isEqualTo(6);
  assertThat(entero(post("?page=2", "{}"), "$.content.length()")).isZero();
 }
 @Test void combinaTodosLosFiltros() throws Exception {
  var respuesta = post("", """
   {"nombre":"TECLADO","precioMin":80,"precioMax":80,
    "nombreCategoria":"INFORMÁTICA","activo":true}
   """);
  assertThat(respuesta.statusCode()).isEqualTo(200);
  assertThat(entero(respuesta, "$.totalElements")).isEqualTo(1);
  assertThat((String) JsonPath.read(respuesta.body(), "$.content[0].nombre")).isEqualTo("Teclado mecánico");
  assertThat((String) JsonPath.read(respuesta.body(), "$.content[0].nombreCategoria")).isEqualTo("Informática");
 }
 @Test void estadoFalseYNulos() throws Exception {
  assertThat(entero(post("", "{\"activo\":false}"), "$.totalElements")).isEqualTo(4);
  assertThat(entero(post("", "{\"activo\":true}"), "$.totalElements")).isEqualTo(12);
  assertThat(entero(post("", "{\"precioMax\":0}"), "$.totalElements")).isEqualTo(1);
  assertThat(entero(post("", "{\"nombre\":\"Producto sin precio\"}"), "$.totalElements")).isEqualTo(1);
  assertThat(entero(post("", "{\"nombreCategoria\":\"Sin productos\"}"), "$.totalElements")).isZero();
 }
 @Test void precioInclusivoYOrdenacion() throws Exception {
  var respuesta = post("?sort=precio,desc", "{\"precioMin\":25,\"precioMax\":35}");
  assertThat(respuesta.statusCode()).isEqualTo(200);
  List<String> nombres = JsonPath.read(respuesta.body(), "$.content[*].nombre");
  assertThat(nombres).containsExactly("Altavoces", "Lámpara", "Ratón inalámbrico");
 }
 @Test void alternativasAYBEquivalentesAC() throws Exception {
  for (String ruta : List.of("/search-a", "/search-b")) {
   var respuesta = get(ruta + "?nombre=TECLADO&precioMin=80&precioMax=80&sort=id,asc");
   assertThat(respuesta.statusCode()).isEqualTo(200);
   assertThat(entero(respuesta, "$.totalElements")).isEqualTo(1);
   assertThat((String) JsonPath.read(respuesta.body(), "$.content[0].nombre")).isEqualTo("Teclado mecánico");
  }
  assertThat(entero(get("/search-b?activo=false"), "$.totalElements")).isEqualTo(4);
 }
 @Test void validacionDelCuerpoYJsonIncorrecto() throws Exception {
  for (String cuerpo : List.of("{\"nombre\":\"a\"}", "{\"precioMin\":-1}",
    "{\"precioMax\":10001}", "{\"precioMin\":\"abc\"}", "{", "null")) {
   error400(post("", cuerpo));
  }
 }
 @Test void validacionDeParametrosYConversionManual() throws Exception {
  for (String ruta : List.of("/search-a", "/search-b")) {
   for (String filtro : List.of("nombre=a", "precioMin=-1", "precioMax=10001", "precioMin=abc")) {
    error400(get(ruta + "?" + filtro));
   }
  }
  error400(get("/search-b?activo=quizas"));
  error400(post("?sort=noExiste,asc", "{}"));
 }
 private void error400(HttpResponse<String> respuesta) {
  assertThat(respuesta.statusCode()).isEqualTo(400);
  assertThat(respuesta.headers().firstValue("content-type").orElse("")).contains("application/problem+json");
  assertThat(entero(respuesta, "$.status")).isEqualTo(400);
 }
 private int entero(HttpResponse<String> respuesta, String ruta) {
  return ((Number) JsonPath.read(respuesta.body(), ruta)).intValue();
 }
 private HttpResponse<String> get(String ruta) throws Exception {
  return cliente.send(HttpRequest.newBuilder(uri(ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
 }
 private HttpResponse<String> post(String query, String cuerpo) throws Exception {
  return cliente.send(HttpRequest.newBuilder(uri("/search" + query))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build(), HttpResponse.BodyHandlers.ofString());
 }
 private URI uri(String ruta) {
  return URI.create("http://localhost:" + port + "/api/v1/productos" + ruta);
 }
}
