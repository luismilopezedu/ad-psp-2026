> Note

# Tutorial Maestro: Paginación y Limitación de Resultados en Spring Data JPA

¡Hola! Como docente, me complace presentarte este tutorial detallado sobre cómo gestionar la **paginación, ordenamiento y limitación de resultados** en Spring Data JPA de manera eficiente y libre de problemas de rendimiento.

Cuando trabajamos con aplicaciones en producción, consultar listas completas de datos directamente de la base de datos es una receta para el desastre: agota la memoria RAM de la JVM, ralentiza la red y degrada la experiencia de usuario [1, 2]. La solución consiste en dividir las grandes colecciones en pequeñas rebanadas (*slices* o páginas) [2, 3].

---

## Modelo de Dominio de Referencia

A lo largo de este tutorial utilizaremos nuestro dominio habitual de **comercio electrónico**:

*   **`Categoria`**: `id`, `nombre`
*   **`Producto`**: `id`, `nombre`, `precio`, `@ManyToOne Categoria`
*   **`Cliente`**: `id`, `nombre`, `email`, `@OneToOne Direccion`
*   **`Pedido`**: `id`, `fecha`, `total`, `estado`, `@ManyToOne Cliente`, `@OneToMany LineaPedido`
*   **`LineaPedido`**: `id`, `cantidad`, `@ManyToOne Producto`

---

## 1. Fundamentos: `Pageable`, `Page` y `Slice`

Spring Data abstrae los parámetros de entrada de paginación a través de la interfaz **`Pageable`** [3]. Para construir peticiones de paginación en código Java utilizamos la implementación **`PageRequest.of(page, size, sort)`** [4]:

*   **`page`**: Número de página deseada (**basado en índice 0 por defecto**: la primera página es la `0`) [4, 5].
*   **`size`**: Cantidad de registros por página [3].
*   **`sort`**: Criterio opcional de ordenación (`Sort.by("precio").descending()`) [4].

### ¿`Page<T>` o `Slice<T>`?

Para el tipo de retorno de las consultas paginadas, Spring Data ofrece dos interfaces principales [6, 7]:

| Tipo de Retorno | Información que proporciona | Comportamiento SQL | Caso de uso ideal |
| :--- | :--- | :--- | :--- |
| **`Page<T>`** | Lista de elementos de la página actual + **número total de elementos y páginas** [7]. | Ejecuta **2 consultas SQL**: la consulta con `LIMIT/OFFSET` + una consulta de conteo (`COUNT`) [1, 8, 9]. | Tablas web con números de página explícitos (ej. `[10] [11] [12] ... [13]`). |
| **`Slice<T>`** | Lista de elementos + **indicador de si existe una página siguiente** (`hasNext()`) [1, 6, 7, 14]. | Ejecuta **1 sola consulta SQL** pidiendo `pageSize + 1` elementos [1]. | Desplazamiento infinito (*infinite scroll*) o botones "Siguiente / Anterior" [3, 14]. |

---

### Ejemplo 1: Paginación con Consultas Derivadas y JPQL (`@Query`)

#### Repositorio (`ProductoRepository.java`)
```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // 1. Consulta derivada paginada (devuelve Page<T>)
    Page<Producto> findByCategoriaNombre(String nombreCategoria, Pageable pageable);

    // 2. Consulta derivada con Slice (no calcula el total de elementos)
    Slice<Producto> findByPrecioGreaterThan(Double precioMinimo, Pageable pageable);

    // 3. Consulta JPQL explícita paginada
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Producto> buscarPorNombrePaginado(@Param("termino") String termino, Pageable pageable);
}
```

#### Invocación en la Capa de Servicio
```java
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public void demoPaginacion() {
        // Solicitamos la página 0 (primera página) con 10 productos ordenados por precio descendente
        Pageable primeraPagina = PageRequest.of(0, 10, Sort.by("precio").descending());

        Page<Producto> pagina = productoRepository.findByCategoriaNombre("Electrónica", primeraPagina);

        System.out.println("Total de elementos en la DB: " + pagina.getTotalElements()); // Requiere el COUNT
        System.out.println("Total de páginas: " + pagina.getTotalPages());
        List<Producto> productos = pagina.getContent(); // 10 productos de la página actual
    }
}
```

---

## 2. Limitación de Resultados: `Limit`, `Top` y `First`

A veces no necesitas navegar por páginas, sino simplemente **obtener un número acotado de registros** (por ejemplo, "los 5 productos más caros") [15, 16].

### A. Palabras Clave Estáticas (`Top` / `First`)
Puedes indicar el límite directamente en el nombre del método [15, 16]:

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Devuelve los primeros 5 productos ordenados por precio descendente
    List<Producto> findTop5ByOrderByPrecioDesc();

    // Devuelve el primer producto que coincida
    Optional<Producto> findFirstByCategoriaNombreOrderByNombreAsc(String categoriaNombre);
}
```

### B. El Parámetro Dinámico `Limit`
En versiones recientes de Spring Data JPA, se introdujo el parámetro **`Limit`** para definir límites de forma dinámica sin acoplarlos al nombre del método [16, 17]:

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Límite dinámico pasado como parámetro
    List<Producto> findByCategoriaNombre(String nombreCategoria, Limit limit, Sort sort);
}
```

```java
// Uso en servicio: obtiene los 3 primeros productos ordenados por precio
List<Producto> top3 = productoRepository.findByCategoriaNombre(
        "Electrónica", 
        Limit.of(3), 
        Sort.by("precio").descending()
);
```

---

## 3. El Gran Peligro: Paginación con `JOIN` y `JOIN FETCH`

Este es uno de los errores de rendimiento más graves al trabajar con JPA/Hibernate.

### El Problema de Fondo
Si intentas paginar una consulta JPQL que incluye un **`JOIN FETCH` sobre una colección** (`@OneToMany` o `@ManyToMany`) [18-20]:

```java
// ❌ CÓDIGO PROBLEMÁTICO
@Query("SELECT p FROM Pedido p JOIN FETCH p.lineas WHERE p.estado = :estado")
Page<Pedido> buscarPedidosConLineasInseguro(@Param("estado") String estado, Pageable pageable);
```

#### ¿Qué sucede internamente?
1. El `JOIN FETCH` sobre la colección de líneas duplica las filas del pedido en la respuesta SQL (producto cartesiano) [18, 21].
2. Si Hibernate aplicara la cláusula SQL `LIMIT` o `OFFSET` directamente sobre esas filas duplicadas, **mutilaría los datos**, cortando las líneas de un pedido a la mitad [18].
3. Para evitar devolver datos corruptos, Hibernate muestra una advertencia crítica en los logs [19, 20]:
   `HHH000104: firstResult/maxResults specified with fetch join; applying in memory!`
4. **Consecuencia fatal:** Hibernate **carga TODA la tabla de la base de datos en la memoria RAM** del servidor Java y realiza el recorte de la página en memoria mediante código [18, 20]. En tablas grandes, esto causa un fallo por falta de memoria (*OutOfMemoryError*).

---

### Soluciones Recomendadas

#### Solución A: Usar `@EntityGraph` en lugar de `JOIN FETCH`
Los Grafos de Entidad permiten indicar las asociaciones que se deben cargar perezosamente sin romper la paginación a nivel de base de datos [22, 23]:

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // ✅ CORRECTO: Usar @EntityGraph con Pageable
    @EntityGraph(attributePaths = {"cliente"})
    Page<Pedido> findByEstado(String estado, Pageable pageable);
}
```

#### Solución B: Declarar una consulta de conteo separada (`countQuery`)
Cuando la consulta JPQL con `JOIN` es compleja, debes indicarle a Spring Data JPA cómo contar los registros de forma eficiente especificando el atributo `countQuery` en `@Query` [24, 25]:

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // ✅ CORRECTO: Consulta principal simple + countQuery explícito sin JOIN FETCH masivo
    @Query(
        value = "SELECT p FROM Pedido p JOIN p.cliente c WHERE c.email LIKE %:domain%",
        countQuery = "SELECT COUNT(p) FROM Pedido p JOIN p.cliente c WHERE c.email LIKE %:domain%"
    )
    Page<Pedido> buscarPorDominioEmailCliente(@Param("domain") String domain, Pageable pageable);
}
```

#### Solución C: Carga en dos pasos o uso de `@BatchSize`
Para colecciones `@OneToMany` (como `Pedido -> LineaPedido`), la recomendación oficial de Hibernate es **paginar la entidad principal sin `JOIN FETCH` sobre la colección** y añadir la anotación **`@BatchSize`** sobre la relación de la entidad [26-28]:

```java
@Entity
public class Pedido {
    // ...
    @OneToMany(mappedBy = "pedido")
    @BatchSize(size = 20) // Carga los hijos en lotes de 20 mediante cláusula IN (?, ?, ...)
    private List<LineaPedido> lineas;
}
```

---

## 4. Paginación con Grafos de Entidad (`@EntityGraph`)

Combinar `@EntityGraph` con paginación es la mejor forma de prevenir excepciones `LazyInitializationException` al tiempo que ejecutamos paginación SQL nativa y limpia.

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Carga la página de Pedidos e inicializa en la misma consulta sus asociaciones Cliente y Direccion
    @EntityGraph(attributePaths = {"cliente", "cliente.direccion"})
    @Query("SELECT p FROM Pedido p WHERE p.total >= :totalMinimo")
    Page<Pedido> buscarPedidosGrandesPaginados(
            @Param("totalMinimo") BigDecimal totalMinimo, 
            Pageable pageable
    );
}
```

---

## 5. Integración con Spring Data Web (Controladores REST)

Spring Boot incluye soporte automático para mapear los parámetros de paginación enviados en la URL de una petición HTTP directamente a un objeto **`Pageable`** dentro de un `@RestController` [29-31].

### Estructura de la URL HTTP GET
La infraestructura `PageableHandlerMethodArgumentResolver` lee automáticamente los siguientes parámetros de la URL [29, 30]:

```http
GET /api/productos?page=0&size=10&sort=precio,desc&sort=nombre,asc
```
*(Nota: No debe haber espacios después de la coma en el parámetro `sort` [32]).*

---

### Ejemplo de Controlador REST con `@PageableDefault`

Podemos definir valores predeterminados para la paginación local usando las anotaciones **`@PageableDefault`** y **`@SortDefault`** [33, 34]:

```java
package com.ejemplo.controlador;

import com.ejemplo.modelo.Producto;
import com.ejemplo.repositorio.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Producto>> listarProductos(
            @PageableDefault(page = 0, size = 12) // Configura tamaño y página por defecto si no vienen en la URL
            @SortDefault(sort = "nombre", direction = Sort.Direction.ASC) // Configura la ordenación por defecto
            Pageable pageable) {

        Page<Producto> pagina = productoRepository.findAll(pageable);
        return ResponseEntity.ok(pagina);
    }
}
```

#### Respuesta JSON Generada por Spring Data
Al devolver un objeto `Page<T>`, Spring serializa la respuesta incluyendo tanto los datos de la página como los metadatos de paginación [7, 35]:

```json
{
  "content": [
    { "id": 1, "nombre": "Teclado Mecánico", "precio": 89.99 },
    { "id": 2, "nombre": "Ratón Óptico", "precio": 29.99 }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 12
  },
  "totalPages": 5,
  "totalElements": 58,
  "last": false,
  "size": 12,
  "number": 0,
  "first": true,
  "numberOfElements": 2
}
```

---

## 6. Cuadro Resumen Didáctico

| Necesidad | Técnica Recomendada | Ventaja Principal |
| :--- | :--- | :--- |
| **Tablas paginadas con navegación exacta** | `Pageable` + Retorno `Page<T>` [1, 6] | Devuelve metadatos completos (`totalElements`, `totalPages`) [7]. |
| **Desplazamiento infinito / Carga diferida** | `Pageable` + Retorno `Slice<T>` [1, 6] | Ahorra la consulta de conteo (`COUNT`), mejorando la velocidad [1]. |
| **Obtener solo los N primeros resultados** | `Limit.of(n)` o palabras `Top`/`First` [15, 16] | Sintaxis limpia sin necesidad de instanciar un objeto `Pageable` [16]. |
| **Paginar cargando entidades relacionadas** | `@EntityGraph` + `Pageable` [22, 23] | Evita el problema de *In-Memory Pagination* (`HHH000104`) [19, 20]. |
| **Recibir paginación desde cliente Web/API** | Parámetro `Pageable` + `@PageableDefault` [29, 34] | Spring Mvc resuelve los parámetros `page`, `size` y `sort` de la URL automáticamente [29, 30]. |

---

💡 ¿Te gustaría preparar un test de integración con `@WebMvcTest` para verificar el comportamiento de los controladores con parámetros de paginación en la URL?