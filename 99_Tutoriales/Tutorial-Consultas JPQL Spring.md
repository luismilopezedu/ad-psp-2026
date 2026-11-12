
# Tutorial Maestro: Consultas JPQL en Spring Data JPA con `@Query`

¡Hola! Te doy la bienvenida a este tutorial práctico y pedagógico mediante el cual vas  a dominar el lenguaje de consultas **JPQL** (*Jakarta Persistence Query Language*) integrado en **Spring Data JPA** mediante la anotación `@Query` [6].

A diferencia de SQL nativo, que trabaja sobre tablas y columnas físicas, **JPQL opera sobre el modelo de objetos (entidades y atributos Java)** [6]. Aunque JPA soporta otros mecanismos de consulta (como `@NamedQuery` o la API de Criteria), en el ecosistema Spring Boot la anotación **`@Query`** sobre las interfaces de repositorio es el estándar preferido por su legibilidad, flexibilidad y mantenibilidad [6].

---

## 1. El Hilo Conductor: Dominio de Comercio Electrónico

Para dar coherencia a todos los ejemplos del tutorial, utilizaremos un modelo de dominio sencillo de un **sistema de comercio electrónico**:

*   **`Categoria`**: Categoría de productos (`id`, `nombre`).
*   **`Producto`**: Artículo a la venta (`id`, `nombre`, `precio`, `categoria` `@ManyToOne`).
*   **`Cliente`**: Usuario que realiza compras (`id`, `nombre`, `email`, `activo`).
*   **`Pedido`**: Orden de compra (`id`, `fecha`, `total`, `estado`, `cliente` `@ManyToOne`).

---

## 2. Consultas Básicas y Filtrado (`WHERE` y Parámetros)

En Spring Data JPA podemos derivar consultas simples a partir del nombre del método (ej. `findByEmail`), pero cuando necesitamos precisión o claridad, definimos la consulta JPQL explícitamente con `@Query` [6].

JPQL permite pasar parámetros de dos formas:
1. **Parámetros posicionales (`?1`, `?2`)**: Coinciden con el orden de los argumentos en el método Java [6].
2. **Parámetros nombrados (`:parametro`)**: Se vinculan mediante la anotación `@Param("parametro")`, lo que hace la consulta más legible y resistente a refactorizaciones de parámetros [6].

### Ejemplo 1: Consulta básica con parámetros nombrados

#### Comparativa: Consulta Derivada vs. JPQL `@Query`

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // 1. OPCIÓN DERIVADA (Spring Data deduce la consulta por el nombre)
    Optional<Cliente> findByEmailAndActivoTrue(String email);

    // 2. OPCIÓN JPQL CON @Query (Consulta explícita y equivalente)
    @Query("SELECT c FROM Cliente c WHERE c.email = :email AND c.activo = true")
    Optional<Cliente> buscarPorEmailActivo(@Param("email") String email);

    // 3. JPQL con parámetros posicionales (?1)
    @Query("SELECT c FROM Cliente c WHERE c.nombre LIKE %?1% AND c.activo = true")
    List<Cliente> buscarActivosPorNombreParcial(String parteNombre);
}
```

> **Explicación pedagógica:**  
> En JPQL, `c` es un **alias** para la entidad `Cliente`. La expresión `SELECT c FROM Cliente c` le indica a JPA que queremos recuperar instancias completas de la entidad `Cliente` cuyo atributo `email` coincida con el parámetro enviado.

---

## 3. Navegación por Relaciones y Joins (`JOIN` vs. `JOIN FETCH`)

Una de las mayores ventajas de JPQL es la capacidad de **navegar a través de los atributos de relación de las entidades** (`@ManyToOne`, `@OneToMany`) usando el operador punto (`.`) o sentencias `JOIN` relacionales [6].

Existen dos tipos fundamentales de `JOIN` en JPQL:
*   **`JOIN` simple (`INNER JOIN` / `LEFT JOIN`)**: Filtra las entidades principales en función de los atributos de la entidad relacionada.
*   **`JOIN FETCH`**: Carga de forma inmediata (*eager*) la asociación diferida (*lazy*) en la misma consulta SQL, evitando el célebre **problema de las N+1 consultas** y las excepciones `LazyInitializationException` fuera de la transacción [4, 6].

### Ejemplo 2: Filtrado por relación y optimización con `JOIN FETCH`

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // A) Consulta derivada con Property Expressions (Navega: Producto -> Categoria -> Nombre)
    List<Producto> findByCategoria_Nombre(String nombreCategoria);

    // B) Equivalente en JPQL usando JOIN explícito
    @Query("SELECT p FROM Producto p JOIN p.categoria c WHERE c.nombre = :nombreCategoria")
    List<Producto> buscarPorNombreCategoria(@Param("nombreCategoria") String nombreCategoria);

    // C) JPQL optimizado con JOIN FETCH
    // Recupera los productos Y inicializa su Categoria en un solo SELECT de SQL
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.precio >= :precioMinimo")
    List<Producto> buscarProductosConCategoriaCargada(@Param("precioMinimo") Double precioMinimo);
}
```

> **Consejo de rendimiento:** Si necesitas acceder a `producto.getCategoria().getNombre()` tras ejecutar la consulta y la relación está mapeada como `FetchType.LAZY`, utiliza **`JOIN FETCH`** para traer toda la información en un único impacto a la base de datos [4].

---

## 4. Operadores de Filtrado y Colecciones (`IN`, `BETWEEN`, `MEMBER OF`)

JPQL incluye operadores avanzados para trabajar con rangos, patrones de texto y colecciones [1, 2].

*   **`IN`**: Comprueba si un atributo coincide con alguno de los elementos de una lista o colección pasada como parámetro [1, 6].
*   **`MEMBER OF`**: Comprueba si un elemento individual pertenece a una colección mapeada en la entidad [1, 2].

### Ejemplo 3: Filtrado por rangos y colecciones

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Pedido;
import com.ejemplo.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Filtrar pedidos entre dos fechas con BETWEEN y estado IN (lista de estados)
    @Query("SELECT p FROM Pedido p WHERE p.fecha BETWEEN :inicio AND :fin AND p.estado IN :estados")
    List<Pedido> buscarPorRangoFechasYEstados(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estados") Collection<String> estados
    );
}
```

---

## 5. Uso de Funciones integradas en JPQL

JPQL ofrece un conjunto completo de funciones escalares y de agregación que pueden emplearse en la cláusula `WHERE`, `HAVING` o `SELECT` [1, 2]:

*   **Funciones de cadena:** `UPPER()`, `LOWER()`, `CONCAT()`, `SUBSTRING()`, `TRIM()`, `LENGTH()` [1, 2].
*   **Funciones numéricas y de fecha:** `ABS()`, `ROUND()`, `MOD()`, `CURRENT_DATE`, `CURRENT_TIME`, `EXTRACT()` [1, 2].
*   **Funciones condicionales para nulos:** `COALESCE(x, y)` (devuelve el primer valor no nulo), `NULLIF(x, y)` [1, 2].

### Ejemplo 4: Búsqueda insensible a mayúsculas y funciones de fecha

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Uso de LOWER, CONCAT y COALESCE para búsqueda de texto flexible
    @Query("SELECT p FROM Producto p " +
           "WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "AND COALESCE(p.precio, 0) > :precioMinimo")
    List<Producto> buscarPorFiltroTextoYPrecio(
            @Param("filtro") String filtro,
            @Param("precioMinimo") Double precioMinimo
    );
}
```

---

## 6. Paginación y Ordenamiento (`Pageable` y `Sort`)

Spring Data JPA simplifica enormemente la paginación de resultados. Podemos pasar un parámetro de tipo **`Pageable`** a un método anotado con `@Query` y devolver un objeto **`Page<T>`** o **`Slice<T>`** [8]. 

Spring Data JPA se encarga automáticamente de inyectar las cláusulas SQL `LIMIT`, `OFFSET` y `ORDER BY` correspondientes a la base de datos subyacente [8].

### Ejemplo 5: Consulta JPQL paginada

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring Data analiza el parámetro Pageable y aplica la paginación a la consulta JPQL
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId")
    Page<Producto> buscarPorCategoriaPaginado(
            @Param("categoriaId") Long categoriaId, 
            Pageable pageable
    );
}
```

#### Uso en la capa de Servicio:
```java
Pageable primeraPagina = PageRequest.of(0, 10, Sort.by("precio").ascending());
Page<Producto> paginaProductos = productoRepository.buscarPorCategoriaPaginado(1L, primeraPagina);
```

---

## 7. Proyecciones DTO con Expresiones de Constructor (`SELECT new ...`)

Hasta ahora, todas nuestras consultas han devuelto **entidades completas** (ej. `List<Producto>`). Sin embargo, en muchas situaciones (como dashboards, reportes o APIs REST) solo necesitamos un subconjunto ligero de campos [6].

JPQL permite instanciar directamente objetos Java que **no son entidades** (los llamados **DTOs** o *Data Transfer Objects*) mediante la sintaxis **`SELECT new com.paquete.MiDTO(...)`** [6].

### Paso 1: Definir la clase DTO (con su constructor coincidente)

```java
package com.ejemplo.dto;

import java.math.BigDecimal;

public class ResumenPedidoDTO {

    private final Long pedidoId;
    private final String nombreCliente;
    private final BigDecimal total;

    // El constructor debe coincidir exactamente en tipo y orden con la consulta JPQL
    public ResumenPedidoDTO(Long pedidoId, String nombreCliente, BigDecimal total) {
        this.pedidoId = pedidoId;
        this.nombreCliente = nombreCliente;
        this.total = total;
    }

    // Getters
    public Long getPedidoId() { return pedidoId; }
    public String getNombreCliente() { return nombreCliente; }
    public BigDecimal getTotal() { return total; }
}
```

### Paso 2: Consulta JPQL utilizando la expresión `new`

```java
package com.ejemplo.repositorio;

import com.ejemplo.dto.ResumenPedidoDTO;
import com.ejemplo.modelo.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Se proyecta el resultado directamente a instancias de ResumenPedidoDTO
    @Query("SELECT new com.ejemplo.dto.ResumenPedidoDTO(p.id, p.cliente.nombre, p.total) " +
           "FROM Pedido p " +
           "WHERE p.estado = :estado")
    List<ResumenPedidoDTO> obtenerResumenesPorEstado(@Param("estado") String estado);
}
```

> **Ventaja principal:** Esta técnica evita cargar entidades gestionadas en el contexto de persistencia, reduciendo la huella de memoria y optimizando las columnas seleccionadas en la cláusula `SELECT` de SQL [6].

---

## Resumen: ¿Cuándo usar Consultas Derivadas vs. `@Query` JPQL?

| Criterio | Consultas Derivadas (`findBy...`) | Consultas `@Query` JPQL |
| :--- | :--- | :--- |
| **Complejidad** | Ideal para consultas sencillas con 1 o 2 condiciones. | Preferible para consultas complejas, con múltiples `JOIN`s o funciones. |
| **Optimización `FETCH`** | Limitado. Puede requerir `@EntityGraph`. | Excelente. Control directo con `JOIN FETCH` [4, 6]. |
| **Proyecciones DTO** | Requiere Interfaces o DTOs con nombres exactos. | Directo y flexible mediante `SELECT new com.package.DTO(...)` [6]. |
| **Mantenibilidad** | Los nombres de métodos muy largos resultan difíciles de leer. | Mantiene el nombre del método Java corto y descriptivo. |

---

# BONUS: Las 10 Consultas JPQL Imprescindibles en Spring Data JPA

A continuación, he seleccionado y desarrollado **10 consultas JPQL fundamentales** que todo programador en el ecosistema Spring Boot debe dominar. Manteniendo nuestro hilo conductor del **dominio de comercio electrónico** (`Categoria`, `Producto`, `Cliente` y `Pedido`), iremos desde lo más esencial hasta patrones avanzados de consulta, optimización y proyección.

---

## El Modelo de Dominio de Referencia

Para recordar la estructura de entidades sobre la que construiremos las consultas:

*   **`Categoria`**: (`id`, `nombre`)
*   **`Producto`**: (`id`, `nombre`, `precio`, `categoria` `@ManyToOne`)
*   **`Cliente`**: (`id`, `nombre`, `email`, `activo`)
*   **`Pedido`**: (`id`, `fecha`, `total`, `estado`, `cliente` `@ManyToOne`)

---

## 1. Selección Básica por Atributos con Parámetros Nombrados

**Caso de uso:** Recuperar una entidad filtrando por coincidencia exacta en uno o más atributos.

Es la consulta más directa. En lugar de usar índices por posición (`?1`), utilizaremos **parámetros nombrados (`:email`)** vinculados con la anotación `@Param`, lo que hace el código expresivo e inmune a cambios en el orden de los argumentos.

```java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c WHERE c.email = :email AND c.activo = true")
    Optional<Cliente> buscarClienteActivoPorEmail(@Param("email") String email);
}
```
> **Lección clave:** `SELECT c FROM Cliente c` selecciona la instancia completa de la entidad `Cliente` (representada por el alias `c`), devolviendo un `Optional<Cliente>`.

---

## 2. Búsqueda de Texto Parcial e Insensible a Mayúsculas/Minúsculas

**Caso de uso:** Buscador de productos donde el usuario introduce un término de búsqueda.

Combinamos las funciones escalares de JPQL **`LOWER()`** y **`CONCAT()`** junto con el operador **`LIKE`** para realizar una búsqueda insensible a mayúsculas/minúsculas de manera segura contra inyecciones.

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorNombreContiene(@Param("termino") String termino);
}
```
> **Lección clave:** `CONCAT('%', :termino, '%')` evalúa los comodines SQL en el servidor JPA de forma limpia, evitando concatenar manualmente signos `%` en la capa de servicio.

---

## 3. Navegación por Asociaciones `@ManyToOne` mediante `JOIN`

**Caso de uso:** Filtrar una entidad principal (`Producto`) según las propiedades de su entidad relacionada (`Categoria`).

En JPQL podemos atravesar relaciones utilizando sentencias `JOIN` explícitas conectando el alias de la entidad principal con el atributo mapeado de su asociación.

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN p.categoria c WHERE c.nombre = :nombreCategoria")
    List<Producto> buscarPorNombreCategoria(@Param("nombreCategoria") String nombreCategoria);
}
```
> **Lección clave:** `p.categoria` es el atributo de relación en `Producto`. Al declarar `JOIN p.categoria c`, creamos el alias `c` para acceder a los campos de `Categoria`.

---

## 4. Carga Eficiente de Asociaciones Diferidas con `JOIN FETCH`

**Caso de uso:** Evitar la excepción `LazyInitializationException` y solucionar el problema de rendimiento de las N+1 consultas al recuperar pedidos y sus clientes.

Por defecto, las relaciones `@ManyToOne` pueden configurarse como `LAZY`. Si intentas acceder a `pedido.getCliente().getNombre()` fuera de la transacción, la aplicación fallará. **`JOIN FETCH`** fuerza a JPA a traer ambas entidades en **una sola consulta SQL**.

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.estado = :estado")
    List<Pedido> buscarPedidosConClienteCargado(@Param("estado") String estado);
}
```
> **Lección clave:** La palabra clave **`FETCH`** le indica al proveedor JPA (Hibernate) que no solo filtre por la asociación, sino que también popule en memoria el objeto `Cliente` dentro de cada entidad `Pedido`.

---

## 5. Filtrado por Colecciones de Valores (`IN`)

**Caso de uso:** Obtener todos los pedidos cuyo estado coincida con cualquiera de los valores de un listado (ej. `"PENDIENTE"`, `"EN_PROCESO"`).

JPQL traduce el operador **`IN`** y una colección Java en una cláusula SQL `IN (?, ?, ...)`.

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.estado IN :estados")
    List<Pedido> buscarPorEstados(@Param("estados") Collection<String> estados);
}
```
> **Lección clave:** Puedes pasar cualquier `Collection<T>` (un `List`, `Set`, etc.) como parámetro de la consulta.

---

## 6. Consulta por Rangos de Fechas y Valores Múltiples (`BETWEEN`)

**Caso de uso:** Generar reportes de ventas en un rango de fechas concreto con un umbral de importe mínimo.

Combinamos el operador **`BETWEEN`** con operadores relacionales estándar (`>=`, `<=`).

```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.fecha BETWEEN :desde AND :hasta AND p.total >= :totalMinimo")
    List<Pedido> buscarPedidosEnRangoYTotal(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("totalMinimo") BigDecimal totalMinimo
    );
}
```
> **Lección clave:** `BETWEEN` es inclusivo en los extremos. JPQL maneja los tipos temporales Java de `java.time` (`LocalDateTime`, `LocalDate`) de forma nativa.

---

## 7. Control de Valores Nulos y Valores por Defecto (`COALESCE`)

**Caso de uso:** Filtrar productos asegurando que los precios nulos no excluyan erróneamente registros.

La función **`COALESCE(expresion, valorPorDefecto)`** evalúa una expresión y, si es `NULL`, devuelve el valor alternativo proporcionado.

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE COALESCE(p.precio, 0) <= :precioMaximo")
    List<Producto> buscarPorPrecioMaximoSeguro(@Param("precioMaximo") BigDecimal precioMaximo);
}
```
> **Lección clave:** `COALESCE` previene que valores nulos en la base de datos arruinen comparaciones lógicas en la cláusula `WHERE`.

---

## 8. Subconsultas y Verificación de Existencia (`EXISTS`)

**Caso de uso:** Encontrar clientes que hayan realizado al menos un pedido cuyo total supere una cierta cantidad.

Las subconsultas correlacionadas con **`EXISTS`** son idóneas cuando queremos filtrar una entidad principal en función de si tiene registros asociados que cumplen una condición compleja, sin necesidad de cargar todos los hijos a memoria.

```java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c WHERE EXISTS (" +
           "  SELECT p FROM Pedido p WHERE p.cliente = c AND p.total > :montoMinimo" +
           ")")
    List<Cliente> buscarClientesConPedidosMayoresA(@Param("montoMinimo") BigDecimal montoMinimo);
}
```
> **Lección clave:** La subconsulta `(SELECT p FROM Pedido p WHERE p.cliente = c ...)` referencia la entidad externa `c`. La condición `EXISTS` devuelve `true` tan pronto como encuentra una coincidencia, siendo sumamente eficiente.

---

## 9. Consultas JPQL Paginadas con `Pageable` y `Page`

**Caso de uso:** Listar productos de una categoría en la interfaz web por páginas de 10 elementos.

Al añadir un parámetro de tipo **`Pageable`** a un método con `@Query`, Spring Data JPA aplica la paginación y ordenamiento automáticamente sobre la consulta JPQL.

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId")
    Page<Producto> buscarPorCategoriaPaginado(
            @Param("categoriaId") Long categoriaId,
            Pageable pageable
    );
}
```
#### Ejemplo de invocación en la capa de servicio:
```java
Pageable paginacion = PageRequest.of(0, 10, Sort.by("nombre").ascending());
Page<Producto> pagina = productoRepository.buscarPorCategoriaPaginado(2L, paginacion);
```
> **Lección clave:** No necesitas escribir `LIMIT` ni `OFFSET` en JPQL. Spring Data calcula las sentencias SQL de paginación y ejecuta la consulta de conteo (`COUNT`) de fondo para construir el objeto `Page<Producto>`.

---

## 10. Proyección DTO con Expresión de Constructor (`SELECT new ...`)

**Caso de uso:** Mostrar una tabla resumen de pedidos en una pantalla de administración sin cargar objetos completos de `Cliente` ni `Pedido`.

Para este escenario, creamos un DTO ligero y utilizamos la sintaxis **`SELECT new com.paquete.DTO(...)`**.

#### Paso A: Definir el DTO (clase inmutable con constructor adecuado)
```java
package com.ejemplo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResumenPedidoDTO {

    private final Long pedidoId;
    private final String nombreCliente;
    private final BigDecimal total;
    private final LocalDateTime fecha;

    public ResumenPedidoDTO(Long pedidoId, String nombreCliente, BigDecimal total, LocalDateTime fecha) {
        this.pedidoId = pedidoId;
        this.nombreCliente = nombreCliente;
        this.total = total;
        this.fecha = fecha;
    }

    // Getters...
    public Long getPedidoId() { return pedidoId; }
    public String getNombreCliente() { return nombreCliente; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getFecha() { return fecha; }
}
```

#### Paso B: La consulta en el repositorio
```java
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT new com.ejemplo.dto.ResumenPedidoDTO(p.id, p.cliente.nombre, p.total, p.fecha) " +
           "FROM Pedido p " +
           "WHERE p.estado = :estado")
    List<ResumenPedidoDTO> obtenerResumenesPorEstado(@Param("estado") String estado);
}
```
> **Lección clave:** El orden y tipo de los argumentos en el `SELECT new` deben coincidir exactamente con los parámetros del constructor del DTO. La consulta devuelve directamente objetos `ResumenPedidoDTO` listos para ser enviados como JSON en una API REST.

---

## Resumen de la Guía de Referencia

| # | Patrón JPQL | Propósito Principal |
| :--- | :--- | :--- |
| **1** | **`WHERE c.email = :email`** | Filtrado exacto con parámetros nombrados. |
| **2** | **`LOWER()` + `LIKE` + `CONCAT()`** | Búsqueda textual flexible e insensible a mayúsculas. |
| **3** | **`JOIN p.categoria c`** | Navegación y filtrado por entidades asociadas. |
| **4** | **`JOIN FETCH p.cliente`** | Prevención del problema N+1 y solución a `LazyInitializationException`. |
| **5** | **`WHERE p.estado IN :estados`** | Filtrado por colecciones de elementos. |
| **6** | **`BETWEEN :desde AND :hasta`** | Consultas por rangos de fechas o valores continuos. |
| **7** | **`COALESCE(p.precio, 0)`** | Manejo seguro de valores nulos con alternativas por defecto. |
| **8** | **`WHERE EXISTS (SELECT ...)`** | Filtrado avanzado basado en subconsultas de presencia. |
| **9** | **`@Query` + `Pageable`** | Paginación y ordenación transparente integrada con Spring Data. |
| **10** | **`SELECT new DTO(...)`** | Proyección directa a objetos ligeros no gestionados. |
