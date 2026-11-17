# Tutorial: Endpoint de Filtrado Avanzado con `PredicateSpecification` en Spring Data JPA

En el desarrollo de aplicaciones empresariales reales, los clientes web y móviles no se conforman con búsquedas simples por un solo campo. Necesitan **paneles de búsqueda multicriterio** (filtrar productos por rango de precios, nombre, categoría, disponibilidad, etc., combinando cualquier número de estos filtros de forma opcional y dinámica).

Para evitar escribir decenas de métodos en el repositorio o consultas JPQL estáticas inmanejables, Spring Data JPA ofrece el mecanismo de **Especificaciones**, que permite trabajar tanto con `Specification` como con **`PredicateSpecification`**. Esta última está disponible desde Spring Data JPA 4.0 y es la utilizada en este proyecto.

En este tutorial aprenderás a construir un endpoint de filtrado avanzado paso a paso, dividido en **tres fases bien delimitadas**:
1. **Recoger los parámetros de filtrado** desde la petición HTTP.
2. **Transformar esos parámetros en criterios de búsqueda** utilizando `PredicateSpecification` y JPA Criteria API.
3. **Ejecutar la consulta paginada** y devolver los resultados.

---

## 1. Fundamentos: Criteria API y `PredicateSpecification`

Antes de programar, es fundamental entender las piezas tecnológicas que intervienen en la generación dinámica de SQL.

### ¿Qué es JPA Criteria API?
Criteria API es la API orientada a objetos nativa de JPA para construir consultas programáticas de forma fuertemente tipada. En lugar de escribir cadenas de texto JPQL, construyes una consulta mediante objetos Java.

Las tres piezas clave de Criteria API son:

*   **`Root<T>`**: Representa la entidad raíz de la consulta (equivale a la cláusula `FROM Producto p`). A través de `root` accedemos a las propiedades de la entidad (ej. `root.get("nombre")`) y podemos realizar navegaciones/joins a entidades asociadas (ej. `root.join("categoria")`).
*   **`CriteriaQuery<?>`**: Representa la estructura global de la consulta (`SELECT`, `GROUP BY`, `ORDER BY`, etc.).
*   **`CriteriaBuilder`**: Es la fábrica de condiciones y expresiones SQL. Nos proporciona métodos como `equal()`, `like()`, `between()`, `greaterThanOrEqualTo()`, `and()`, `or()`, etc.

### ¿Qué es `PredicateSpecification`?

`PredicateSpecification<T>` es una interfaz funcional para construir **predicados reutilizables independientes del tipo de consulta**. Coexiste con `Specification<T>`; no es un simple cambio de nombre.

Su contrato esencial es el siguiente (esquema explicativo; **no hay que crear esta interfaz en el proyecto**):

```java
@FunctionalInterface
public interface PredicateSpecification<T> {
    Predicate toPredicate(From<?, T> from, CriteriaBuilder builder);
}
```

La implementación recibe **dos argumentos**:

- `From<?, T>`: representa el origen desde el que se accede a los atributos y se realizan joins. Puede ser una raíz o un join. En el código lo llamamos `root`, pero su tipo sigue siendo `From<?, Producto>`.
- `CriteriaBuilder`: permite construir las condiciones.

> No recibe un `CriteriaQuery<?>`. La firma de tres argumentos `(root, query, builder)` pertenece a `Specification<T>`, no a `PredicateSpecification<T>`.

| Interfaz | Argumentos de la lambda | Uso en este tutorial |
| --- | --- | --- |
| `Specification<T>` | `(root, query, builder)` | Referencia para distinguir las dos APIs |
| `PredicateSpecification<T>` | `(root, builder)` | Construcción de los filtros y ejecución con `findBy` |

Spring Data se ocupa de construir la consulta. Nuestra especificación aporta su condición WHERE; la paginación y la ordenación se aplican al ejecutarla.

---

## 2. Hilo Conductor: Modelo de Dominio de Referencia

Para mantener la coherencia con nuestros tutoriales anteriores, utilizaremos la entidad **`Producto`** asociada a **`Categoria`**:

```java
package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false)
 private String nombre;
 
 @Column(precision = 12, scale = 2)
 private BigDecimal precio;
 
 private Boolean activo;
 
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "categoria_id", nullable = false)
 private Categoria categoria;
 
 protected Producto() {}
 
 public Producto(String nombre, BigDecimal precio, Categoria categoria) {
  this(nombre, precio, true, categoria);
 }
 public Producto(String nombre, BigDecimal precio, Boolean activo, Categoria categoria) {
  this.activo = activo;
  this.nombre = nombre;
  this.precio = precio;
  this.categoria = categoria;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public BigDecimal getPrecio() { return precio; }
 public Boolean getActivo() { return activo; }
 public void setNombre(String nombre) { this.nombre = nombre; }
 public void setPrecio(BigDecimal precio) { this.precio = precio; }
 public void setActivo(Boolean activo) { this.activo = activo; }
 public void setCategoria(Categoria categoria) { this.categoria = categoria; }
 public Categoria getCategoria() { return categoria; }
 @Override public String toString() { return "Producto[id=" + id + ", nombre=" + nombre + ", precio=" + precio + "]"; }
}
```

```java
package com.ejemplo.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categorias")
public class Categoria {
 
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false, unique = true)
 private String nombre;
 
 protected Categoria() {}
 
 public Categoria(String nombre) {
  this.nombre = nombre;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 @Override public String toString() { return "Categoria[id=" + id + ", nombre=" + nombre + "]"; }
}
```

---

## 3. PASO 1: Recoger los Parámetros de Filtrado (3 Alternativas)

Para recibir las condiciones que el usuario desea aplicar desde la capa Web (Spring MVC / REST), existen tres enfoques principales. Analizaremos las ventajas, inconvenientes y capacidades de validación de cada uno.

---

Los fragmentos de controlador de este apartado pertenecen a una clase con `@RequestMapping("/api/v1/productos")` y `@Validated`. Las rutas de cada método son relativas a ese prefijo. El controlador completo aparece en el paso 3.

### Alternativa A: Parámetros individuales con `@RequestParam`

Es la forma tradicional de recibir parámetros por la URL de una petición `GET`.

```java
@GetMapping("/search-a")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaA(
   @RequestParam(required = false) @Size(min = 2, max = 50) String nombre,
   @RequestParam(required = false) @DecimalMin("0.0") BigDecimal precioMin,
   @RequestParam(required = false) @DecimalMax("10000.0") BigDecimal precioMax,
   @RequestParam(required = false) String nombreCategoria,
   @PageableDefault(size = 10) Pageable pageable) {
  
  ProductoFilterDTO filtro = new ProductoFilterDTO();
  filtro.setNombre(nombre);
  filtro.setPrecioMin(precioMin);
  filtro.setPrecioMax(precioMax);
  filtro.setNombreCategoria(nombreCategoria);
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }
```

*   **Ventajas:**
    *   Muy claro y auto-documentado en OpenAPI / Swagger.
    *   Cumple de forma estricta la semántica HTTP (`GET`).
    *   Tipado automático en los argumentos Java (`BigDecimal`, `String`, etc.).
*   **Inconvenientes:**
    *   La firma del método en el controlador se vuelve enorme e inmanejable si hay más de 5 o 6 campos de filtro.
*   **Validación:**
    *   Permite anotar cada parámetro directamente con reglas de Bean Validation (`@Size`, `@DecimalMin`). Requiere colocar `@Validated` sobre la clase `@RestController`.

---

En A se conserva la firma del tutorial original: cuatro filtros, sin `activo`. B y C admiten los cinco filtros.

### Alternativa B: `@RequestParam` mapeado a `Map` o `MultiValueMap`

Recoge todos los parámetros dinámicos de la URL en un mapa clave-valor.

```java
@GetMapping("/search-b")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaB(
   @RequestParam MultiValueMap<String, String> queryParams,
   @PageableDefault(size = 10) Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(parser.parsear(queryParams), pageable));
 }
```

*   **Ventajas:**
    *   Firma del controlador compacta. Recoge todas las claves recibidas, pero cada filtro admitido requiere lógica de interpretación y una condición de búsqueda.
*   **Inconvenientes:**
    *   **Pérdida total de seguridad de tipos**: Todos los valores son recabados como cadenas (`String`), obligando a realizar conversiones manuales (`new BigDecimal(valor)`).
    *   Susceptible a errores tipográficos imprevistos por parte del cliente HTTP.
*   **Validación:**
    *   `@Valid` sobre el mapa no valida automáticamente las reglas de los campos del DTO. En esta implementación, `ProductoFilterParser` realiza la conversión y la validación manual.

---

#### Conversión y validación manual: `ProductoFilterParser.java`

El mapa recoge también `page`, `size` y `sort`, pero el parser solo interpreta los cinco filtros conocidos. Spring resuelve la paginación por separado. Se toma el primer valor de cada filtro y se ignoran las claves desconocidas. Para `activo` se admiten únicamente `true` y `false`, sin distinguir mayúsculas; así se evita que un texto incorrecto se convierta silenciosamente en `false`.

```java
package com.ejemplo.servicio;

import com.ejemplo.dto.ProductoFilterDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import java.math.BigDecimal;

/** Alternativa B: conversión y validación manual, tomando el primer valor de cada filtro. */
@Component
public class ProductoFilterParser {
 public ProductoFilterDTO parsear(MultiValueMap<String, String> params) {
  ProductoFilterDTO filtro = new ProductoFilterDTO();
  String nombre = params.getFirst("nombre");
  if (nombre != null && (nombre.length() < 2 || nombre.length() > 50)) {
   throw new IllegalArgumentException("El nombre de búsqueda debe tener entre 2 y 50 caracteres");
  }
  filtro.setNombre(nombre);
  filtro.setPrecioMin(decimal(params.getFirst("precioMin"), "precioMin"));
  filtro.setPrecioMax(decimal(params.getFirst("precioMax"), "precioMax"));
  if (filtro.getPrecioMin() != null && filtro.getPrecioMin().signum() < 0) {
   throw new IllegalArgumentException("El precio mínimo no puede ser negativo");
  }
  if (filtro.getPrecioMax() != null && filtro.getPrecioMax().compareTo(new BigDecimal("10000.0")) > 0) {
   throw new IllegalArgumentException("El precio máximo superado");
  }
  filtro.setNombreCategoria(params.getFirst("nombreCategoria"));
  String activo = params.getFirst("activo");
  if (activo != null) {
   if (!activo.equalsIgnoreCase("true") && !activo.equalsIgnoreCase("false")) {
    throw new IllegalArgumentException("activo debe ser true o false");
   }
   filtro.setActivo(Boolean.valueOf(activo));
  }
  return filtro;
 }
 private BigDecimal decimal(String valor, String campo) {
  if (valor == null) return null;
  try {
   return new BigDecimal(valor);
  } catch (NumberFormatException ex) {
   throw new IllegalArgumentException(campo + " debe ser un número decimal válido");
  }
 }
}
```

---

### Alternativa C: `@RequestBody` con un DTO de Filtrado (Vía `POST`)

Encapsula todo el criterio de búsqueda dentro de un objeto Java (DTO) enviado como JSON en el cuerpo de la petición.

#### El DTO de Filtrado (`ProductoFilterDTO.java`):
```java
package com.ejemplo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductoFilterDTO {

    @Size(min = 2, max = 50, message = "El nombre de búsqueda debe tener entre 2 y 50 caracteres")
    private String nombre;

    @DecimalMin(value = "0.0", message = "El precio mínimo no puede ser negativo")
    private BigDecimal precioMin;

    @DecimalMax(value = "10000.0", message = "El precio máximo superado")
    private BigDecimal precioMax;

    private String nombreCategoria;

    private Boolean activo;

    // Getters y Setters...
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioMin() { return precioMin; }
    public void setPrecioMin(BigDecimal precioMin) { this.precioMin = precioMin; }
    public BigDecimal getPrecioMax() { return precioMax; }
    public void setPrecioMax(BigDecimal precioMax) { this.precioMax = precioMax; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
```

#### En el Controlador:
```java
@PostMapping("/search")
 public ResponseEntity<Page<ProductoDTO>> filtrarProductos(
   @Valid @RequestBody ProductoFilterDTO filtro,
   @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }
```

*   **Ventajas:**
    *   Estructurado, fuertemente tipado y sumamente limpio.
    *   **Soporte pleno de Bean Validation**: Al utilizar `@Valid @RequestBody`, Spring valida el objeto completo de forma automática antes de tocar la capa de negocio.
    *   Permite filtros complejos con listas anidadas u objetos compuestos.
*   **Inconvenientes:**
    *   Utiliza `POST` para una búsqueda con cuerpo JSON; no ofrece las mismas propiedades de caché y de URL compartible que una petición `GET`.

---

### Tabla Comparativa de Alternativas de Entrada

| Criterio | A) `@RequestParam` individual | B) `@RequestParam` `Map` | C) `@RequestBody` DTO |
| :--- | :--- | :--- | :--- |
| **Método HTTP** | `GET` | `GET` | `POST` |
| **Limpieza de Código** | Baja (muchos parámetros) | Alta | Excelente |
| **Tipado Fuerte** | Sí | No (`String`) | Sí |
| **Validación Bean Validation** | Sí (con `@Validated`) | No (Manual) | **Excelente (`@Valid`)** |
| **Uso adecuado** | Pocos filtros conocidos | Parámetros que se interpretan manualmente | Filtros estructurados enviados como JSON |

---

> BONUS: Se espera que las próximas versiones de Spring Boot permitan atender peticiones con el nuevo verbo  `QUERY`([RFC 10008](https://datatracker.ietf.org/doc/rfc10008/)) que semánticamente sí que permite realizar peticiones similares a `GET` pero con datos en el cuerpo. Ya hay algunas librerías de terceros que permiten usar, en Spring Boot 4.1, anotaciones como `HttpQueryMapping` para implementar endpoints que atiendan a ese verbo HTTP.

## 4. PASO 2: Transformar Parámetros en Criterios (`PredicateSpecification`)

Una vez recogidos los datos en nuestro `ProductoFilterDTO`, el segundo paso consiste en construir una **especificación dinámica**. 

Crearemos una clase utilitaria que construya instancias de `PredicateSpecification<Producto>` combinando condiciones mediante `CriteriaBuilder`.

### Navegación por Asociaciones en Specifications
Para filtrar productos según el nombre de su **`Categoria`** (asociación `@ManyToOne`), debemos realizar un `JOIN` a nivel de Criteria API:

```java
Join<Producto, Categoria> categoriaJoin = root.join("categoria", JoinType.INNER);
Predicate condicionCategoria = builder.equal(categoriaJoin.get("nombre"), nombreCategoria);
```

### Clase de Construcción de Especificaciones (`ProductoSpecifications.java`)

```java
package com.ejemplo.especificacion;

import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.modelo.Categoria;
import com.ejemplo.modelo.Producto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoSpecifications {

    /**
     * Construye una PredicateSpecification dinámica basada en los campos no nulos del DTO.
     */
    public static PredicateSpecification<Producto> conFiltroDinamico(ProductoFilterDTO filtro) {
        return (root, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro por Nombre parcial (LIKE %nombre%)
            if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("nombre")),
                        "%" + filtro.getNombre().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            // 2. Filtro por Rango de Precio Mínimo (>=)
            if (filtro.getPrecioMin() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("precio"), filtro.getPrecioMin()));
            }

            // 3. Filtro por Rango de Precio Máximo (<=)
            if (filtro.getPrecioMax() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("precio"), filtro.getPrecioMax()));
            }

            // 4. Filtro por Estado Activo
            if (filtro.getActivo() != null) {
                predicates.add(builder.equal(root.get("activo"), filtro.getActivo()));
            }

            // 5. NAVEGACIÓN POR ASOCIACIÓN: Filtro por Nombre de Categoria (@ManyToOne)
            if (filtro.getNombreCategoria() != null && !filtro.getNombreCategoria().isBlank()) {
                // Realiza un JOIN explícito desde Producto hacia Categoria
                Join<Producto, Categoria> categoriaJoin = root.join("categoria", JoinType.INNER);
                predicates.add(builder.equal(
                        builder.lower(categoriaJoin.get("nombre")),
                        filtro.getNombreCategoria().toLowerCase(Locale.ROOT)
                ));
            }

            // Combina todos los predicados activos mediante un AND lógico
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

> LEGACY BONUS: En [este enlace](https://github.com/lmlopezmagana/ad-psp-24-25/blob/main/09_RestQueryLang/src/main/java/com/salesianostriana/dam/restquerylang/query/GenericSpecificationBuilder.java) puedes encontrar una ejemplo (que hace uso de `Specification` y no de `PredicateSpecification`)  mediante el cual, en lugar de crear una clase específica para traducir los parámetros de filtrado de cada entidad, se podrían generalizar.

---

## 5. PASO 3: Ejecutar la Consulta y Devolver Resultados Paginados

### 1. El Repositorio (`ProductoRepository.java`)
Para que nuestro repositorio de Spring Data JPA pueda ejecutar especificaciones, debe heredar de **`JpaSpecificationExecutor<T>`**:

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    // La búsqueda paginada se ejecuta con findBy(spec, query -> query.page(pageable)).
}
```

---

El bloque anterior muestra solo la declaración necesaria para este tutorial.

### 2. La Capa de Servicio (`ProductoService.java`)
El servicio recibe el DTO de filtro y el objeto de paginación, construye la especificación y ejecuta la consulta:

```java
package com.ejemplo.servicio;

import com.ejemplo.dto.ProductoDTO;
import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.especificacion.ProductoSpecifications;
import com.ejemplo.modelo.Producto;
import com.ejemplo.repositorio.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarProductosFiltro(ProductoFilterDTO filtro, Pageable pageable) {
        // 1. Construir la especificacion de predicados
        PredicateSpecification<Producto> spec = ProductoSpecifications.conFiltroDinamico(filtro);

        // 2. Ejecutar la consulta paginada en el repositorio
        Page<Producto> paginaEntidades = productoRepository.findBy(spec, query -> query.page(pageable));

        // 3. Mapear los resultados a DTO de respuesta
        // Este mapeo también lo podríamos hacer en el controlador en función del fetching de las asociaciones.
        return paginaEntidades.map(this::convertirADto);
    }

    private ProductoDTO convertirADto(Producto p) {
        String nombreCat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getPrecio(), p.getActivo(), nombreCat);
    }
}
```

---

#### Cómo funciona `findBy`

```java
Page<Producto> paginaEntidades = productoRepository.findBy(
    spec,
    query -> query.page(pageable)
);
```

1. `spec` aporta las condiciones construidas con `PredicateSpecification<Producto>`.
2. `query` es el objeto de consulta fluida que proporciona Spring Data; **no es un `CriteriaQuery`**.
3. `query.page(pageable)` ejecuta la búsqueda con página, tamaño y ordenación, y devuelve `Page<Producto>`.
4. `paginaEntidades.map(this::convertirADto)` transforma el contenido sin perder los metadatos de paginación.

> No se utiliza `findAll(spec, pageable)`: esa sobrecarga recibe `Specification`, no `PredicateSpecification`.

La conversión a DTO ocurre dentro de `@Transactional(readOnly = true)`. Así se puede leer el nombre de la categoría LAZY con `spring.jpa.open-in-view=false`. El JOIN del filtro no es un JOIN FETCH: filtrar por categoría no implica cargarla anticipadamente.

#### DTO de salida: `ProductoDTO.java`

```java
package com.ejemplo.dto;
import java.math.BigDecimal;

public class ProductoDTO {
 private final Long id;
 private final String nombre;
 private final BigDecimal precio;
 private final Boolean activo;
 private final String nombreCategoria;
 public ProductoDTO(Long id, String nombre, BigDecimal precio, Boolean activo, String nombreCategoria) {
  this.id = id;
  this.nombre = nombre;
  this.precio = precio;
  this.activo = activo;
  this.nombreCategoria = nombreCategoria;
 }
 public Long getId() { return id; }
 public String getNombre() { return nombre; }
 public BigDecimal getPrecio() { return precio; }
 public Boolean getActivo() { return activo; }
 public String getNombreCategoria() { return nombreCategoria; }
}
```

### 3. El Controlador REST (`ProductoController.java`)

```java
package com.ejemplo.controlador;

import com.ejemplo.dto.ProductoDTO;
import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.servicio.ProductoService;
import com.ejemplo.servicio.ProductoFilterParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@Validated
@RequestMapping("/api/v1/productos")
public class ProductoController {
 private final ProductoService productoService;
 private final ProductoFilterParser parser;
 public ProductoController(ProductoService productoService, ProductoFilterParser parser) {
  this.productoService = productoService;
  this.parser = parser;
 }
 @GetMapping("/search-a")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaA(
   @RequestParam(required = false) @Size(min = 2, max = 50) String nombre,
   @RequestParam(required = false) @DecimalMin("0.0") BigDecimal precioMin,
   @RequestParam(required = false) @DecimalMax("10000.0") BigDecimal precioMax,
   @RequestParam(required = false) String nombreCategoria,
   @PageableDefault(size = 10) Pageable pageable) {

  ProductoFilterDTO filtro = new ProductoFilterDTO();
  filtro.setNombre(nombre);
  filtro.setPrecioMin(precioMin);
  filtro.setPrecioMax(precioMax);
  filtro.setNombreCategoria(nombreCategoria);
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }

 @GetMapping("/search-b")
 public ResponseEntity<Page<ProductoDTO>> buscarAlternativaB(
   @RequestParam MultiValueMap<String, String> queryParams,
   @PageableDefault(size = 10) Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(parser.parsear(queryParams), pageable));
 }
 @PostMapping("/search")
 public ResponseEntity<Page<ProductoDTO>> filtrarProductos(
   @Valid @RequestBody ProductoFilterDTO filtro,
   @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
  return ResponseEntity.ok(productoService.buscarProductosFiltro(filtro, pageable));
 }
}
```

---

## 6. Validación Sistemática de Entradas y Gestión de Errores

Las tres alternativas delegan en el mismo servicio, pero validan las entradas en puntos diferentes:

| Alternativa | Validación | Gestión del error |
| --- | --- | --- |
| A | Restricciones en los parámetros y `@Validated` en la clase | `ConstraintViolationException` y errores de conversión de Spring MVC |
| B | Conversión y comprobaciones en `ProductoFilterParser` | `IllegalArgumentException` |
| C | `@Valid @RequestBody ProductoFilterDTO` | `MethodArgumentNotValidException`; un JSON ilegible se trata mediante Spring MVC |

La implementación utiliza `@RestControllerAdvice` y extiende `ResponseEntityExceptionHandler`. Los errores cubiertos producen HTTP 400 con `ProblemDetail`; los errores de validación incluyen una propiedad con los suberrores como solemos hacer habitualmente.

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

 @ExceptionHandler(IllegalArgumentException.class)
 public ProblemDetail filtroIncorrecto(IllegalArgumentException ex) {
  return problema(ex.getMessage());
 }

 @ExceptionHandler(PropertyReferenceException.class)
 public ProblemDetail ordenIncorrecto(PropertyReferenceException ex) {
  return problema("Propiedad de ordenación desconocida: " + ex.getPropertyName());
 }
 private ProblemDetail problema(String detalle) {
  ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
  problema.setTitle("Petición incorrecta");
  return problema;
 }

 @ExceptionHandler(ConstraintViolationException.class)
 public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
  ProblemDetail result = ProblemDetail
          .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

  List<ApiValidationSubError> subErrors =
          ex.getConstraintViolations().stream()
                  .map(ApiValidationSubError::from)
                  .toList();

  result.setProperty("invalid-params", subErrors);

  return result;

 }

 @Override
 protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
  ProblemDetail result = ProblemDetail
          .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Error de validación");

  List<ApiValidationSubError> subErrors =
          ex.getAllErrors().stream()
                  .map(ApiValidationSubError::from)
                  .toList();

  result.setProperty("invalid-params", subErrors);


  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(result);
 }


 @Builder
 record ApiValidationSubError(
         String object,
         String message,
         @JsonInclude(JsonInclude.Include.NON_NULL)
         String field,
         @JsonInclude(JsonInclude.Include.NON_NULL)
         Object rejectedValue
 ) {

  public ApiValidationSubError(String object, String message) {
   this(object, message, null, null);
  }

  public static ApiValidationSubError from(ObjectError error) {

   ApiValidationSubError result = null;

   if (error instanceof FieldError fieldError) {
    result = ApiValidationSubError.builder()
            .object(fieldError.getObjectName())
            .message(fieldError.getDefaultMessage())
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .build();
   } else {
    result = ApiValidationSubError.builder()
            .object(error.getObjectName())
            .message(error.getDefaultMessage())
            .build();
   }

   return result;


  }

  public static ApiValidationSubError from(ConstraintViolation v) {
   return ApiValidationSubError.builder()
           .message(v.getMessage())
           .rejectedValue(v.getInvalidValue())
           .object(v.getRootBeanClass().getSimpleName())
           .field(getFieldName(v))
           .build();
  }

  /*
      Método que permite obtener el nombre del campo sobre
      el que se ha producido la violación
   */
  private static String getFieldName(ConstraintViolation<?> v) {

   String field = null;

   for (Path.Node node : v.getPropertyPath()) {
    if (node.getKind() == ElementKind.PROPERTY || node.getKind() == ElementKind.PARAMETER) {
     field = node.getName() != null ? node.getName() : node.toString();
    }
   }
   return field;
  }
 }

}

```

### Alcance exacto de las validaciones

Se mantienen las reglas del código fuente:

- `nombre`, cuando está presente, debe tener entre 2 y 50 caracteres.
- `precioMin` debe ser mayor o igual que 0.
- `precioMax` debe ser menor o igual que 10000.
- Los campos no son obligatorios; C acepta `{}` para buscar sin filtros.
- No existe una regla cruzada que compare `precioMin` y `precioMax`. Un rango invertido devuelve una página vacía.
- No se añade un mínimo a `precioMax` ni un máximo a `precioMin`.
- La especificación ignora nombres y categorías en blanco; la validación del nombre se aplica antes. Por ejemplo, `""` falla `@Size`, mientras que dos espacios cumplen la longitud y después no añaden condición.
- B rechaza un decimal vacío o no numérico. A usa la conversión de Spring MVC y C la deserialización JSON; no se presupone que los tres mecanismos acepten exactamente las mismas representaciones textuales.

## 7. Resumen de Flujo Completo

1. El controlador recoge los filtros mediante parámetros individuales, un mapa o un DTO JSON.
2. Se validan y, cuando corresponde, se convierten los valores.
3. `ProductoService` recibe `ProductoFilterDTO` y `Pageable`.
4. `ProductoSpecifications.conFiltroDinamico` genera los predicados y los combina con AND; solo crea el JOIN si se filtra por categoría.
5. `productoRepository.findBy(spec, query -> query.page(pageable))` ejecuta la consulta paginada.
6. El servicio convierte las entidades a `ProductoDTO` dentro de la transacción.
7. El controlador devuelve `Page<ProductoDTO>` como JSON; los errores de entrada se gestionan mediante el advice.

## 8. Puesta en Marcha y Datos de Ejemplo

```bash
./mvnw spring-boot:run
```

En Windows, utiliza `mvnw.cmd`. En macOS/Linux, si es necesario, ejecuta antes `chmod +x mvnw`. Se necesita un JDK 21 completo y acceso a las dependencias Maven.

Para usar el perfil PostgreSQL incluido:

```bash
docker compose up -d --wait
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

El perfil utiliza por defecto `localhost:5433/jpql`, con usuario y contraseña `jpql`, y permite sobrescribir `DB_URL`, `DB_USER` y `DB_PASSWORD`. Ambos entornos usan `create-drop`: el esquema es de práctica y se recrea.

### Ajuste del `DataSeed`

La carga conserva `@Component`, `@PostConstruct` y la transacción explícita con `TransactionTemplate` del proyecto base. Mantiene los 16 productos: 12 de Informática y 4 de Hogar, además de una categoría vacía «Sin productos». De ellos, 12 están activos y 4 inactivos: Webcam, Router, Mesa y Producto sin precio.

El constructor previo asigna `activo = true`; el nuevo constructor permite indicar el estado. Ejemplos exactos de la carga:

```java
new Producto("Teclado mecánico", new BigDecimal("80.00"), informatica)
new Producto("Webcam", new BigDecimal("45.00"), false, informatica)
new Producto("Producto sin precio", null, false, hogar)
new Producto("Muestra gratuita", BigDecimal.ZERO, hogar)
```

Son expresiones individuales dentro de la lista de carga, no sentencias independientes. Los datos permiten probar precios cero y nulos, rangos, estados y varias páginas sin modificar las consultas JPQL anteriores.

## 9. Peticiones para Practicar

Las tres rutas admiten `page`, `size` y `sort` en la URL. La primera página es 0 y el tamaño predeterminado es 10. C ordena por nombre por defecto; A y B no especifican orden predeterminado. Para obtener un orden estable, usa `sort=id,asc`.

### C: combinar los cinco filtros

```bash
curl -X POST 'http://localhost:8080/api/v1/productos/search?page=0&size=10&sort=precio,asc' \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"TECLADO","precioMin":80,"precioMax":80,"nombreCategoria":"INFORMÁTICA","activo":true}'
```

Resultado esperado: un producto, Teclado mecánico, con precio 80.00, activo y categoría Informática. Los límites de precio son inclusivos.

### A: parámetros individuales

```bash
curl 'http://localhost:8080/api/v1/productos/search-a?nombre=TECLADO&precioMin=80&precioMax=80'
```

### B: mapa y conversión manual

```bash
curl 'http://localhost:8080/api/v1/productos/search-b?activo=false&sort=id,asc'
```

Resultado esperado: los cuatro productos inactivos.

### C: segunda página sin filtros

```bash
curl -X POST 'http://localhost:8080/api/v1/productos/search?page=1&size=10' \
  -H 'Content-Type: application/json' -d '{}'
```

Resultado esperado: seis elementos en esta página, 16 en total y dos páginas.

| Cuerpo JSON de C | Total esperado |
| --- | ---: |
| `{}` | 16 |
| `{"activo":true}` | 12 |
| `{"activo":false}` | 4 |
| `{"nombreCategoria":"INFORMÁTICA"}` | 12 |
| `{"precioMin":25,"precioMax":35}` | 3 |
| `{"precioMax":0}` | 1 |
| `{"nombreCategoria":"Sin productos"}` | 0 |

El precio nulo no satisface comparaciones numéricas. `precioMax = 0` devuelve únicamente Muestra gratuita; no aplica el COALESCE de otras consultas JPQL del proyecto.

Nombre y categoría se comparan sin distinguir mayúsculas, utilizando `Locale.ROOT` en la conversión Java. No se eliminan las tildes. El nombre usa coincidencia parcial y la categoría igualdad; `%` y `_` conservan su significado como comodines LIKE.

### Comprobar errores

```bash
curl -i -X POST 'http://localhost:8080/api/v1/productos/search' \
  -H 'Content-Type: application/json' -d '{"nombre":"a","precioMin":-1}'
curl -i 'http://localhost:8080/api/v1/productos/search-a?nombre=a'
curl -i 'http://localhost:8080/api/v1/productos/search-b?precioMin=abc'
```

Se espera HTTP 400 y un cuerpo `ProblemDetail`. El proyecto incluye más peticiones en `docs/peticiones.http`.

## 10. Comprobación y Referencias

```bash
./mvnw clean test
```
 
- [API de PredicateSpecification](https://docs.spring.io/spring-data/jpa/reference/api/java/org/springframework/data/jpa/domain/PredicateSpecification.html).
- [API de JpaSpecificationExecutor](https://docs.spring.io/spring-data/jpa/reference/api/java/org/springframework/data/jpa/repository/JpaSpecificationExecutor.html).


# BONUS: ¿Qué pasa con el fetching dinámico y las especificaciones?

Manejar el **fetching dinámico** (la estrategia de carga de asociaciones) al realizar consultas basadas en **`PredicateSpecification`** o **`Specification`** es perfectamente posible y una práctica muy habitual en Spring Data JPA.

Existen **dos estrategias principales** para resolver este reto: el uso de **Grafos de Entidad (`@EntityGraph`)** y el **`fetch()` dinámico mediante la API de Criteria**.

---

### Estrategia 1: Fetching Dinámico con Criteria API (`from.fetch(...)`)

La interfaz **`PredicateSpecification<T>`** (introducida en Spring Data JPA 4.0) recibe como primer argumento un objeto de tipo **`From<?, T> from`**. Dado que `From` extiende la interfaz `FetchParent` de JPA Criteria, es posible invocar directamente el método **`.fetch()`** dentro de la especificación para realizar un **`LEFT JOIN FETCH`** o **`INNER JOIN FETCH`** programático.

#### 1. Crear Especificaciones dedicadas al Fetching
Puedes encapsular la lógica de carga en métodos estáticos de utilidad:

```java
package com.ejemplo.especificacion;

import com.ejemplo.modelo.Producto;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.PredicateSpecification;

public class ProductoFetchSpecifications {

    // Carga la asociación @ManyToOne "categoria"
    public static PredicateSpecification<Producto> fetchCategoria() {
        return (from, builder) -> {
            from.fetch("categoria", JoinType.LEFT);
            return null; // Devuelve null para no añadir ninguna restricción al WHERE
        };
    }

    // Carga la colección @ManyToMany "etiquetas"
    public static PredicateSpecification<Producto> fetchEtiquetas() {
        return (from, builder) -> {
            from.fetch("etiquetas", JoinType.LEFT);
            return null;
        };
    }
}
```

#### 2. Combinar Filtros y Fetching en la Capa de Servicio
Al ser `PredicateSpecification` una interfaz funcional, puedes encadenar la especificación de filtro con la de carga usando el método `.and()`:

```java
@Transactional(readOnly = true)
public List<Producto> buscarProductosConFetch(ProductoFilterDTO filtro) {
    // 1. Especificación con las condiciones WHERE
    PredicateSpecification<Producto> specFiltro = ProductoSpecifications.conFiltroDinamico(filtro);

    // 2. Combinación con el plan de fetching dinámico
    PredicateSpecification<Producto> specCompleta = specFiltro
            .and(ProductoFetchSpecifications.fetchCategoria())
            .and(ProductoFetchSpecifications.fetchEtiquetas());

    // 3. Ejecución de la consulta
    return productoRepository.findAll(specCompleta);
}
```

> ⚠️ **Advertencia sobre Paginación (`Pageable`) y `COUNT`**:  
> Si utilizas la interfaz clásica `Specification<T>` (`Root<T> root, CriteriaQuery<?> query, ...`) en lugar de `PredicateSpecification`, al realizar consultas paginadas (`findAll(spec, pageable)`), Spring Data ejecuta dos consultas: la de datos y la de conteo (`COUNT`). En la consulta de conteo, hacer un `.fetch()` incondicional puede provocar un error en JPA. Para evitarlo en la `Specification` clásica, se debe verificar el tipo de retorno antes de aplicar el fetch:
> ```java
> if (query.getResultType() != Long.class && query.getResultType() != long.class) {
>     root.fetch("categoria", JoinType.LEFT);
> }
> ```

---

### Estrategia 2: Combinar `PredicateSpecification` con Grafos de Entidad (`@EntityGraph`)

Si prefieres separar la responsabilidad de **filtrar** (en la `Specification`) de la responsabilidad de **cargar datos** (en el plan de lectura), puedes utilizar **Grafos de Entidad (`@EntityGraph`)**.

#### Opción A: Sobrescribir el método en la interfaz del Repositorio
Puedes declarar la anotación `@EntityGraph` directamente sobre los métodos de `JpaSpecificationExecutor` en tu repositorio:

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    // Sobrescribe findAll(PredicateSpecification) aplicando un Grafo Ad-hoc
    @Override
    @EntityGraph(attributePaths = {"categoria", "etiquetas"})
    List<Producto> findAll(PredicateSpecification<Producto> spec);

    // Sobrescribe findAll con paginación aplicando el Grafo de forma segura
    @Override
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findAll(PredicateSpecification<Producto> spec, Pageable pageable);
}
```

#### Opción B: Uso de la API Fluida (`SpecificationFluentQuery` / `findBy`)
A partir de Spring Data JPA 3.0 / 4.0, `JpaSpecificationExecutor` incluye el método de consulta fluida **`findBy(spec, q -> q...)`**. Esto permite aplicar ordenamiento, límites y proyecciones de forma dinámica sobre la especificación:

```java
Page<ProductoDTO> pagina = productoRepository.findBy(specFiltro, q -> q
        .as(ProductoDTO.class) // Proyección dinámica
        .page(PageRequest.of(0, 10, Sort.by("nombre")))
);
```

---

### Cuadro Comparativo de Estrategias

| Criterio | Fetching en Criteria (`from.fetch()`) | `@EntityGraph` en Repositorio | Fluent API (`findBy`) |
| :--- | :--- | :--- | :--- |
| **Flexibilidad** | **Máxima**: Se decide campo por campo en tiempo de ejecución. | **Media**: Ligada a la firma del repositorio o grafo declarado. | **Alta**: Orientada a proyecciones y paginación fluida. |
| **Desacoplamiento** | La `Specification` contiene lógica de predicado y de fetch. | Separa las condiciones `WHERE` del plan de lectura. | Abstrae la ejecución de la consulta. |
| **Seguridad con Paginación** | Requiere cuidado en la `Specification` clásica con la query de `COUNT`. | **Excelente**: Spring Data gestiona la consulta de conteo sin colisiones. | **Excelente**: Diseñada específicamente para `Page` y `Slice`. |

---

💡 **Recomendación de arquitectura:**  
* Si el cliente HTTP solicita cargar relaciones opcionales (ej. `?include=categoria,etiquetas`), utiliza **`from.fetch()`** dentro de `PredicateSpecification`.  
* Si buscas una carga homogénea y limpia para listados paginados, combina **`PredicateSpecification` con `@EntityGraph`** en el repositorio.