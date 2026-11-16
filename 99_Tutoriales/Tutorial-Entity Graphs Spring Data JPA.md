> Note

# Tutorial Maestro: Grafos de Entidad (Entity Graphs) en Spring Data JPA

¡Hola! Como docente, es un placer presentarte este tutorial dedicado a una de las herramientas más potentes y elegantes que ofrece JPA 2.1 y Spring Data JPA para resolver los problemas de rendimiento derivados de la carga de asociaciones: los **Grafos de Entidad (`Entity Graphs`)**.

---

## 1. El Problema de Fondo: N+1 Consultas y `LazyInitializationException`

En una arquitectura limpia orientada a objetos mapeada con JPA/Hibernate, la regla de oro es configurar todas las asociaciones como **`FetchType.LAZY`** por defecto. Esto evita arrastrar media base de datos a la memoria RAM cada vez que leemos una entidad simple.

Sin embargo, esta estrategia plantea dos grandes retos en producción:

1. **El dilema de la `LazyInitializationException`**: Ocurre cuando intentas acceder a una relación `LAZY` (por ejemplo, `pedido.getLineas()`) fuera del ámbito de una transacción o sesión de Hibernate abierta (por ejemplo, en la capa de controlador o en la serialización JSON de una API REST).
2. **El problema de las N+1 consultas**: Si dentro de un bucle transaccional recorres 100 pedidos y accedes a sus clientes `LAZY`, Hibernate ejecutará 1 consulta inicial para traer los pedidos más 100 consultas adicionales para traer cada cliente (101 consultas SQL en total).

### ¿Por qué los Grafos de Entidad superan a `JOIN FETCH`?
Aunque podemos usar `JOIN FETCH` en JPQL, esto presenta inconvenientes:
* Acopla la lógica de consulta en cadenas JPQL extensas.
* Dificulta la reutilización de métodos derivados o métodos estándar de `JpaRepository` (como `findAll()` o `findById()`).
* Puede generar problemas al combinar múltiples colecciones de tipo `List` en un solo `JOIN FETCH` (produciendo el error `MultipleBagFetchException`).

Los **Grafos de Entidad (`Entity Graphs`)** actúan como una **plantilla de lectura dinámica**: nos permiten alterar el plan de carga (*fetching plan*) **solo para una consulta concreta**, sobreescribiendo temporalmente la configuración `LAZY` de las entidades sin tocar el código del modelo.

---

## 2. El Modelo de Dominio Extendido

Para ilustrar escenarios reales con relaciones de diversa cardinalidad (`@OneToOne`, `@OneToMany`, `@ManyToOne` y `@ManyToMany`), ampliaremos nuestro dominio de **comercio electrónico**:

```
[Direccion] <--(1:1)-- [Cliente] --(1:N)--> [Pedido] --(1:N)--> [LineaPedido] --(N:1)--> [Producto] --(N:1)--> [Categoria]
                                                                                               |
                                                                                            (N:M)
                                                                                               v
                                                                                           [Etiqueta]
```

### Código fuente de las Entidades

#### 1. Entidad `Direccion` (`@OneToOne` inversa)
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "direcciones")
public class Direccion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String calle;
    private String ciudad;
    private String codigoPostal;

    // Getters y setters...
}
```

#### 2. Entidad `Cliente` (`@OneToOne` y `@OneToMany`)
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    // Getters y setters...
}
```

#### 3. Entidad `Etiqueta` y `Categoria`
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    // Getters y setters...
}

@Entity
@Table(name = "etiquetas")
public class Etiqueta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    // Getters y setters...
}
```

#### 4. Entidad `Producto` (`@ManyToOne` y `@ManyToMany`)
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "producto_etiquetas",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private Set<Etiqueta> etiquetas = new HashSet<>();

    // Getters y setters...
}
```

#### 5. Entidad `LineaPedido` (`@ManyToOne` compuesta)
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lineas_pedido")
public class LineaPedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // Getters y setters...
}
```

#### 6. Entidad `Pedido` (Entidad principal)
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LineaPedido> lineas = new ArrayList<>();

    // Getters y setters...
}
```

---

## 3. Grafos de Entidad Ad-hoc (`@EntityGraph(attributePaths = ...)`)

Los grafos ad-hoc o declarativos son aquellos que se definen **directamente en los métodos del repositorio** mediante la anotación `@EntityGraph`. Son ideales cuando necesitas una solución rápida y localizada sin sobrecargar la entidad de dominio con anotaciones.

### Ejemplo 1: Grafo Simple de Primer Nivel

Imagina que necesitas cargar un `Cliente` junto con su `Direccion` (`@OneToOne`) en una sola consulta SQL para mostrar su perfil de envío.

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Indica a JPA que trate el atributo 'direccion' como EAGER solo para esta consulta
    @EntityGraph(attributePaths = {"direccion"})
    Optional<Cliente> findWithDireccionByEmail(String email);
}
```

#### SQL Generado por Hibernate:
```sql
SELECT c.id, c.email, c.nombre, c.direccion_id,
       d.id, d.calle, d.ciudad, d.codigoPostal
FROM clientes c
LEFT OUTER JOIN direcciones d ON c.direccion_id = d.id
WHERE c.email = ?;
```

---

### Ejemplo 2: Subgrafos Implícitos mediante Rutas Compuestas (Navegación Profunda)

¿Qué ocurre si al consultar un `Pedido` queremos cargar no solo la colección `lineas`, sino también el `producto` de cada línea y la `categoria` de cada producto?

Con `attributePaths` podemos definir rutas anidadas utilizando la **notación de punto (`.`)**, lo que Spring Data JPA traduce internamente como **subgrafos**:

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // 1. Aplicado a un método derivado por nombre
    // Carga: Pedido -> cliente -> direccion AND Pedido -> lineas -> producto -> categoria
    @EntityGraph(attributePaths = {
        "cliente.direccion",
        "lineas.producto.categoria"
    })
    List<Pedido> findByEstado(String estado);

    // 2. Aplicado a la sobrescripción de un método estándar de JpaRepository
    @Override
    @EntityGraph(attributePaths = {"cliente", "lineas.producto"})
    Optional<Pedido> findById(Long id);

    // 3. Aplicado a una consulta con @Query explícita
    @EntityGraph(attributePaths = {"lineas.producto.etiquetas"})
    @Query("SELECT p FROM Pedido p WHERE p.total > :minimo")
    List<Pedido> buscarPedidosGrandesConEtiquetas(@Param("minimo") java.math.BigDecimal minimo);
}
```

---

## 4. Grafos de Entidad con Nombre (`@NamedEntityGraph` y `@NamedSubgraph`)

Cuando una estructura de lectura se vuelve compleja o se utiliza repetidamente en múltiples repositorios, la mejor práctica es **declarar un Grafo de Entidad con Nombre** directamente sobre la clase `@Entity`.

### Estructura de las Anotaciones:
*   **`@NamedEntityGraph`**: Define el grafo raíz sobre la entidad.
    *   `name`: Nombre identificativo del grafo.
    *   `attributeNodes`: Lista de atributos simples o de primer nivel que se deben cargar (`@NamedAttributeNode`).
    *   `subgraphs`: Subgrafos declarados para profundizar en las relaciones compuestas (`@NamedSubgraph`).
*   **`@NamedSubgraph`**: Especifica la carga de atributos dentro de una entidad relacionada.

---

### Ejemplo Completo: Definición de Grafo Completo en `Pedido.java`

Queremos definir un grafo reutilizable llamado `"Pedido.completoConDetalles"` que cargue:
1. El `cliente` del pedido y la `direccion` de ese cliente.
2. Las `lineas` del pedido, el `producto` de cada línea, la `categoria` del producto y sus `etiquetas` (`@ManyToMany`).

#### Modificación en la Entidad `Pedido`:

```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
// DEFINICIÓN DEL GRAFO REUTILIZABLE
@NamedEntityGraph(
    name = "Pedido.completoConDetalles",
    attributeNodes = {
        // Atributo simple con subgrafo para llegar a la dirección del cliente
        @NamedAttributeNode(value = "cliente", subgraph = "subgrafo-cliente-direccion"),
        // Atributo de colección con subgrafo para navegar en profundidad por las líneas
        @NamedAttributeNode(value = "lineas", subgraph = "subgrafo-lineas-producto")
    },
    subgraphs = {
        // Subgrafo 1: Cliente -> Direccion
        @NamedSubgraph(
            name = "subgrafo-cliente-direccion",
            attributeNodes = {
                @NamedAttributeNode("direccion")
            }
        ),
        // Subgrafo 2: LineaPedido -> Producto
        @NamedSubgraph(
            name = "subgrafo-lineas-producto",
            attributeNodes = {
                @NamedAttributeNode(value = "producto", subgraph = "subgrafo-producto-detalles")
            }
        ),
        // Subgrafo 3: Producto -> Categoria y Etiquetas
        @NamedSubgraph(
            name = "subgrafo-producto-detalles",
            attributeNodes = {
                @NamedAttributeNode("categoria"),
                @NamedAttributeNode("etiquetas")
            }
        )
    }
)
public class Pedido {
    // Campos y relaciones mapeados anteriormente...
}
```

---

### Uso del Grafo con Nombre en el Repositorio

Una vez definido en la entidad, hacer uso de él en el repositorio de Spring Data JPA es tan sencillo como hacer referencia a su nombre mediante el atributo `value` de `@EntityGraph`:

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Se invoca al grafo declarado previamente en la entidad Pedido
    @EntityGraph(value = "Pedido.completoConDetalles")
    Optional<Pedido> findCompletoById(Long id);

    @EntityGraph(value = "Pedido.completoConDetalles")
    List<Pedido> findCompletoByClienteId(Long clienteId);
}
```

---

## 5. Modos de Ejecución: `EntityGraphType.FETCH` vs `EntityGraphType.LOAD`

La anotación `@EntityGraph` tiene un parámetro clave llamado `type` que acepta dos valores de la enumeración `EntityGraphType`:

```java
@EntityGraph(value = "Pedido.completoConDetalles", type = EntityGraphType.FETCH)
```

| Modo de Grafo | Comportamiento con los atributos ESPECIFICADOS en el Grafo | Comportamiento con los atributos NO ESPECIFICADOS en el Grafo |
| :--- | :--- | :--- |
| **`EntityGraphType.FETCH`** *(Valor por defecto)* | Se cargan como **`EAGER`**. | Se tratan como **`LAZY`** (incluso si en la entidad estaban mapeados estáticamente como `FetchType.EAGER`). |
| **`EntityGraphType.LOAD`** | Se cargan como **`EAGER`**. | **Respetan su configuración estática** original definida en las entidades Java (`EAGER` o `LAZY`). |

> **Recomendación didáctica:** Mantén el valor por defecto (`FETCH`). Esto te garantiza un control absoluto y predecible de la consulta SQL generada, evitando que relaciones declaradas por descuido como `EAGER` se cuelen en la lectura.

---

## 6. Cuadro Comparativo de Estrategias de Fetching

Para ayudarte a decidir qué técnica utilizar en cada caso de uso en Spring Data JPA:

| Criterio | JPQL `JOIN FETCH` | `@EntityGraph(attributePaths)` | `@NamedEntityGraph` | Proyecciones DTO (`SELECT new`) |
| :--- | :--- | :--- | :--- | :--- |
| **Ubicación** | En la anotación `@Query`. | En el repositorio. | En la `@Entity`. | En la `@Query` o interfaz DTO. |
| **Complejidad de lectura** | Media. | Alta (con notación de punto). | Muy Alta (con `@NamedSubgraph`). | Alta. |
| **Reutilización** | Baja (ligada a la consulta). | Alta. | Muy Alta (global). | Media. |
| **Entidades Gestionadas** | Sí (devuelve entidades). | Sí (devuelve entidades). | Sí (devuelve entidades). | No (devuelve objetos planos de solo lectura). |
| **Caso de Uso Ideal** | Consultas ad-hoc con lógica de filtrado compleja. | Métodos derivados o de repositorio estándar. | Estructuras de carga complejas y recurrentes. | Reportes, vistas web y APIs de solo lectura. |

---

### Resumen Didáctico

1. **`FetchType.LAZY` en el modelo:** Configura siempre tus entidades con la carga perezosa predeterminada.
2. **`@EntityGraph(attributePaths = {...})` para el 80% de los casos:** Utiliza grafos ad-hoc para resolver la carga perezosa en métodos de repositorio simples o derivados.
3. **`@NamedEntityGraph` + `@NamedSubgraph` para grafos profundos:** Emplea grafos declarativos con nombre cuando la profundidad de navegación supere los 2 niveles y se repita en varias partes del sistema.

---
💡 *¿Te gustaría preparar una batería de pruebas de integración con `@DataJpaTest` para verificar el número de consultas SQL emitidas por Hibernate con y sin Grafos de Entidad?*