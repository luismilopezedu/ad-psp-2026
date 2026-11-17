# 07_Filtrado

Continuación de `04_ConsultasJPQL` para el tutorial de filtrado avanzado con **PredicateSpecification**. Java 21 y Spring Boot 4.1.2, conservados del proyecto original.

## Arranque

Requisitos: un JDK 21 completo y acceso a Maven Central. Se incluye Maven Wrapper.

```bash
./mvnw spring-boot:run
```

En Windows: `mvnw.cmd spring-boot:run`. En macOS/Linux, si es necesario: `chmod +x mvnw`.

La API queda disponible en `http://localhost:8080`. Por defecto usa H2 en memoria, con carga de datos mediante `@Component` y `@PostConstruct`. La carga se ejecuta dentro de la transacción explícita que ya utilizaba el proyecto base. El esquema se recrea en cada arranque.

Para usar el perfil PostgreSQL original:

```bash
docker compose up -d --wait
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Conexión predeterminada: `localhost:5433/jpql`, usuario y contraseña `jpql`. Se pueden sobrescribir mediante `DB_URL`, `DB_USER` y `DB_PASSWORD`. También este perfil utiliza `create-drop`: es una base de prácticas.

## Rutas del tutorial

| Alternativa | Método y ruta | Entrada |
| --- | --- | --- |
| A | GET `/api/v1/productos/search-a` | Parámetros individuales: nombre, precioMin, precioMax, nombreCategoria |
| B | GET `/api/v1/productos/search-b` | MultiValueMap, conversión y validación manual; admite también activo |
| C | POST `/api/v1/productos/search` | ProductoFilterDTO como JSON con los cinco filtros |

Se conserva la firma de A del tutorial, que no incluye `activo`. B y C permiten filtrar por ese campo. Todos los filtros son opcionales y se combinan con AND.

Las tres rutas aceptan `page`, `size` y `sort` en la URL. La primera página es la 0 y el tamaño predeterminado es 10. C ordena por nombre de forma predeterminada; A/B mantienen la configuración del tutorial, sin orden predeterminado. Usa `sort=id,asc` si necesitas un orden estable en A/B.

```bash
curl -X POST 'http://localhost:8080/api/v1/productos/search?page=0&size=10&sort=precio,asc' \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"TECLADO","precioMin":80,"precioMax":80,"nombreCategoria":"INFORMÁTICA","activo":true}'
```

Resultado esperado: un producto, `Teclado mecánico`, de 80.00 euros, activo y de la categoría Informática.

```bash
curl 'http://localhost:8080/api/v1/productos/search-a?nombre=TECLADO&precioMin=80&precioMax=80'
curl 'http://localhost:8080/api/v1/productos/search-b?activo=false&sort=id,asc'
```

Más ejemplos, incluidos errores y páginas sucesivas, en `docs/peticiones.http` (IntelliJ HTTP Client o extensión REST Client de VS Code).

## Componentes añadidos o ajustados

- `Producto`: propiedad Boolean `activo`, accesores y constructor de cuatro argumentos; se conserva el constructor previo, que asigna `true`.
- `ProductoRepository`: incorpora `JpaSpecificationExecutor<Producto>` y conserva las consultas JPQL.
- `ProductoFilterDTO` y `ProductoDTO`: entrada validada y respuesta sin serializar entidades JPA.
- `ProductoSpecifications`: cinco condiciones opcionales, incluida la navegación con JOIN hacia Categoria.
- `ProductoService`: consulta paginada y transformación a DTO dentro de una transacción de solo lectura. `open-in-view` permanece desactivado.
- `ProductoFilterParser`: conversión y validación manual para la alternativa B.
- `ProductoController`: las tres alternativas completas.
- `GlobalExceptionHandler`: extiende `ResponseEntityExceptionHandler`; devuelve ProblemDetail para errores de validación, conversión, JSON y ordenación desconocida.
- Dependencias web MVC y Bean Validation; se elimina el modo de aplicación sin servidor web.

Las clases anteriores de clientes, pedidos, repositorios y demo JPQL se conservan. La demo de consola queda desactivada por defecto; puede activarse con `--app.demo.enabled=true`.

## Datos para practicar

Se conservan los 16 productos del proyecto base: 12 de Informática y 4 de Hogar, además de la categoría vacía «Sin productos». Hay 12 activos y 4 inactivos: Webcam, Router, Mesa y Producto sin precio. Los precios incluyen 0 y null, y permiten comprobar rangos inclusivos. Los 16 resultados sin filtro y los 12 de Informática permiten recorrer varias páginas de tamaño 10.

| Filtro en C | Total esperado |
| --- | ---: |
| `{}` | 16 |
| `{"activo":true}` | 12 |
| `{"activo":false}` | 4 |
| `{"nombreCategoria":"INFORMÁTICA"}` | 12 |
| `{"precioMin":25,"precioMax":35}` | 3 |
| `{"precioMax":0}` | 1 |
| `{"nombreCategoria":"Sin productos"}` | 0 |

El precio nulo no satisface comparaciones numéricas. La búsqueda por nombre es parcial; la categoría se compara por igualdad. Ambas ignoran mayúsculas/minúsculas, pero no eliminan tildes. El patrón LIKE mantiene la semántica del tutorial: `%` y `_` actúan como comodines.

## Correcciones acordadas respecto al tutorial

`PredicateSpecification` recibe dos argumentos (`From<?, T>` y `CriteriaBuilder`), no los tres de `Specification`:

```java
return (root, builder) -> { /* construcción de predicados */ };
```

La paginación utiliza la API fluida:

```java
Page<Producto> paginaEntidades = productoRepository.findBy(spec, query -> query.page(pageable));
```

Referencias: [PredicateSpecification](https://docs.spring.io/spring-data/jpa/reference/api/java/org/springframework/data/jpa/domain/PredicateSpecification.html) y [JpaSpecificationExecutor](https://docs.spring.io/spring-data/jpa/reference/api/java/org/springframework/data/jpa/repository/JpaSpecificationExecutor.html).

Se mantienen las validaciones del tutorial: nombre de 2 a 50 caracteres, precioMin >= 0 y precioMax <= 10000. No se añaden reglas cruzadas de precios; un rango invertido devuelve una página vacía. En B se toma el primer valor de cada filtro, se ignoran claves desconocidas y `activo` acepta true/false sin distinguir mayúsculas. Un decimal presente pero vacío no es válido en B.

## Pruebas

```bash
./mvnw clean test
```

Se incluyen seis tests JPQL heredados y siete tests HTTP nuevos con H2. **No se pudieron ejecutar en el entorno de generación por falta de acceso a Maven Central.** Detalle en `docs/Verificacion.md`.
