# Tutorial Maestro: Endpoint de Filtrado Avanzado con `PredicateSpecification` en Spring Data JPA

En el desarrollo de aplicaciones empresariales reales, los clientes web y móviles no se conforman con búsquedas simples por un solo campo. Necesitan **paneles de búsqueda multicriterio** (filtrar productos por rango de precios, nombre, categoría, disponibilidad, etc., combinando cualquier número de estos filtros de forma opcional y dinámica).

Para evitar escribir decenas de métodos en el repositorio o consultas JPQL estáticas inmanejables, Spring Data JPA ofrece el mecanismo de **Especificaciones**, renovado en versiones recientes con la API **`PredicateSpecification`**.

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
`PredicateSpecification<T>` es la evolución moderna y funcional de la interfaz clásica `Specification<T>` de Spring Data JPA (disponible en la API renovada de Spring Data JPA). Define una interfaz funcional cuya responsabilidad es retornar un objeto `Predicate` de la Criteria API:

```java
@FunctionalInterface
public interface PredicateSpecification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}
```

Gracias a su naturaleza de interfaz funcional, permite definir reglas de filtrado reutilizables mediante expresiones Lambda o métodos de utilidad estáticos.

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private BigDecimal precio;

    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Constructores, Getters y Setters...
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
```

```java
package com.ejemplo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // Getters y Setters...
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

---

## 3. PASO 1: Recoger los Parámetros de Filtrado (3 Alternativas)

Para recibir las condiciones que el usuario desea aplicar desde la capa Web (Spring MVC / REST), existen tres enfoques principales. Analizaremos las ventajas, inconvenientes y capacidades de validación de cada uno.

---

### Alternativa A: Parámetros individuales con `@RequestParam`

Es la forma tradicional de recibir parámetros por la URL de una petición `GET`.

```java
@GetMapping("/api/v1/productos/search-a")
public ResponseEntity<Page<ProductoDTO>> buscarAlternativaA(
        @RequestParam(required = false) @Size(min = 2, max = 50) String nombre,
        @RequestParam(required = false) @DecimalMin("0.0") BigDecimal precioMin,
        @RequestParam(required = false) @DecimalMax("10000.0") BigDecimal precioMax,
        @RequestParam(required = false) String nombreCategoria,
        @PageableDefault(size = 10) Pageable pageable) {
    // ...
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

### Alternativa B: `@RequestParam` mapeado a `Map` o `MultiValueMap`

Recoge todos los parámetros dinámicos de la URL en un mapa clave-valor.

```java
@GetMapping("/api/v1/productos/search-b")
public ResponseEntity<Page<ProductoDTO>> buscarAlternativaB(
        @RequestParam MultiValueMap<String, String> queryParams,
        @PageableDefault(size = 10) Pageable pageable) {
    
    String nombre = queryParams.getFirst("nombre");
    String precioMinStr = queryParams.getFirst("precioMin");
    // ...
}
```

*   **Ventajas:**
    *   Firma del controlador ultra limpia e infinitamente flexible. Acepta cualquier número de filtros sin modificar código Java.
*   **Inconvenientes:**
    *   **Pérdida total de seguridad de tipos**: Todos los valores son recabados como cadenas (`String`), obligando a realizar conversiones manuales (`BigDecimal.valueOf(...)`).
    *   Susceptible a errores tipográficos imprevistos por parte del cliente HTTP.
*   **Validación:**
    *   **No soporta Bean Validation estándar** (`@Valid`). Requiere implementar parseo y validación de errores de forma imperativa y manual dentro del servicio.

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
@PostMapping("/api/v1/productos/search")
public ResponseEntity<Page<ProductoDTO>> buscarAlternativaC(
        @Valid @RequestBody ProductoFilterDTO filtro,
        @PageableDefault(size = 10) Pageable pageable) {
    // ...
}
```

*   **Ventajas:**
    *   Estructurado, fuertemente tipado y sumamente limpio.
    *   **Soporte pleno de Bean Validation**: Al utilizar `@Valid @RequestBody`, Spring valida el objeto completo de forma automática antes de tocar la capa de negocio.
    *   Permite filtros complejos con listas anidadas u objetos compuestos.
*   **Inconvenientes:**
    *   Rompe ligeramente la semántica purista de HTTP GET al utilizar `POST` para una operación de lectura.

---

### Tabla Comparativa de Alternativas de Entrada

| Criterio | A) `@RequestParam` individual | B) `@RequestParam` `Map` | C) `@RequestBody` DTO |
| :--- | :--- | :--- | :--- |
| **Método HTTP** | `GET` | `GET` | `POST` |
| **Limpieza de Código** | Baja (muchos parámetros) | Alta | Excelente |
| **Tipado Fuerte** | Sí | No (`String`) | Sí |
| **Validación Bean Validation** | Sí (con `@Validated`) | No (Manual) | **Excelente (`@Valid`)** |
| **Recomendación** | Filtros sencillos (1-3 campos) | Búsqueda totalmente libre | **Filtros avanzados empresariales** |

---

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

public class ProductoSpecifications {

    /**
     * Construye una PredicateSpecification dinámica basada en los campos no nulos del DTO.
     */
    public static PredicateSpecification<Producto> conFiltroDinamico(ProductoFilterDTO filtro) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro por Nombre parcial (LIKE %nombre%)
            if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("nombre")),
                        "%" + filtro.getNombre().toLowerCase() + "%"
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
                        filtro.getNombreCategoria().toLowerCase()
                ));
            }

            // Combina todos los predicados activos mediante un AND lógico
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

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
    // Hereda métodos como:
    // Page<Producto> findAll(Specification<Producto> spec, Pageable pageable);
}
```

---

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
        Page<Producto> paginaEntidades = productoRepository.findAll(spec, pageable);

        // 3. Mapear los resultados a DTO de respuesta
        return paginaEntidades.map(this::convertirADto);
    }

    private ProductoDTO convertirADto(Producto p) {
        String nombreCat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getPrecio(), p.getActivo(), nombreCat);
    }
}
```

---

### 3. El Controlador REST (`ProductoController.java`)

```java
package com.ejemplo.controlador;

import com.ejemplo.dto.ProductoDTO;
import com.ejemplo.dto.ProductoFilterDTO;
import com.ejemplo.servicio.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductoDTO>> filtrarProductos(
            @Valid @RequestBody ProductoFilterDTO filtro,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {

        Page<ProductoDTO> resultado = productoService.buscarProductosFiltro(filtro, pageable);
        return ResponseEntity.ok(resultado);
    }
}
```

---

## 6. Validación Sistemática de Entradas y Gestión de Errores

Para que el sistema sea robusto, cualquier incorrección en los parámetros de filtrado enviados por el cliente (por ejemplo, enviar un precio negativo o un nombre demasiado corto) debe ser capturada **de forma sistemática y centralizada**.

Aquí podemos aplicar todo lo que hemos aprendido sobre  `ProblemDetail` y su uso desde una clase anotada con `@RestControllerAdvice` y que extienda a `ResponseEntityExceptionHandler`.

---

## 7. Resumen de Flujo Completo

```text
[ Cliente HTTP ]
       │
       │ 1. POST /api/v1/productos/search (JSON + Pageable en URL)
       ▼
[ ProductoController ] ─────── ( @Valid valida el DTO )
       │                                 │ (Si es inválido -> GlobalExceptionHandler)
       │ 2. Pasa DTO válido y Pageable   ▼
       ▼
[ ProductoService ]
       │
       │ 3. Invoca ProductoSpecifications.conFiltroDinamico(filtro)
       ▼
[ Criteria API / PredicateSpecification ] ─── (Genera Predicates dinámicos + JOIN a Categoria)
       │
       │ 4. productoRepository.findAll(spec, pageable)
       ▼
[ Base de Datos ] ────────── (Ejecuta SELECT ... FROM productos p INNER JOIN categorias c ... LIMIT ?)
       │
       ▼
[ Respuesta JSON Paginada (Page<ProductoDTO>) ]
```
