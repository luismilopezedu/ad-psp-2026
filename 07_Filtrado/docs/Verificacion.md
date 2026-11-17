# Verificación de 07_Filtrado

## Comprobaciones realizadas

- Revisados los fragmentos del tutorial y su integración con el proyecto base.
- Corregidas la firma de PredicateSpecification y la consulta paginada según la API de Spring Data JPA.
- Conservados los seis tests de consultas JPQL del proyecto original.
- Añadidos siete tests HTTP de integración en FiltradoHttpTest: paginación, combinación de los cinco filtros, estado false, precio nulo/cero, límites inclusivos, ordenación, alternativas A/B y errores de entrada en las tres rutas.
- Revisado el ZIP para excluir target, metadatos de macOS y archivos del IDE.

## Límite de la verificación

Se intentó ejecutar `./mvnw test`, pero Maven no pudo resolver el POM padre Spring Boot 4.1.2: `Unknown host repo.maven.apache.org`. Las dependencias no estaban en la caché local. Por tanto, **esta iteración no se ha compilado ni se han ejecutado sus pruebas** en el entorno de generación. El perfil PostgreSQL tampoco se ha ejecutado.

El runtime disponible tampoco contiene `lib/ct.sym`; para verificar localmente se necesita un JDK 21 completo y acceso a Maven Central. No se ha cambiado la versión del proyecto base para sortear estas limitaciones.

## Verificación local

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Los tests usan H2 y un servidor HTTP en puerto aleatorio; no necesitan Docker. Para comprobar PostgreSQL, sigue el README y ejecuta las peticiones de `docs/peticiones.http`.
