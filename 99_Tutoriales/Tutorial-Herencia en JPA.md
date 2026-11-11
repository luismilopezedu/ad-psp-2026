> Note

# Tutorial Completo: Estrategias de Herencia en JPA, Hibernate y Spring Data JPA

¡Hola! Como docente y arquitecto de software, he preparado este tutorial exhaustivo para enseñarte a dominar las **4 estrategias de herencia** que soportan JPA y Hibernate al interactuar con **Spring Data JPA**.

Modelar la herencia es uno de los mayores retos al trabajar con ORM debido al **desajuste de paradigmas** (*impedance mismatch*): las bases de datos relacionales no soportan el concepto de herencia de clases de forma nativa [1, 2]. Para tender un puente entre la Orientación a Objetos y el modelo relacional, JPA y Hibernate ofrecen cuatro estrategias principales [1, 3].

---

## 1. `@MappedSuperclass` (Herencia solo en el Modelo de Clases)

### ¿En qué consiste?
Con `@MappedSuperclass`, la estructura de herencia existe **únicamente en el modelo de objetos de Java** y no se refleja a nivel de tablas en la base de datos [1, 4]. 
No existe una tabla propia para la superclase; en su lugar, cada tabla de las entidades hijas incluye tanto sus propios campos como los atributos heredados de la superclase [4].

> **Punto clave:** Al no existir una tabla base ni un mapeo de entidad para la superclase, **no es posible realizar consultas polimórficas** directamente sobre la superclase [4].

### Ejemplo Completo de Código

#### Superclase Base (`BaseEntity.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass // (1) Se marca como superclase mapeada (NO es una @Entity)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCreacion;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
```

#### Subclases Concretas (`CuentaDebito.java` y `CuentaCredito.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class CuentaDebito extends BaseEntity {

    private BigDecimal comisionDescubierto;

    // Getters y setters...
}

@Entity
public class CuentaCredito extends BaseEntity {

    private BigDecimal limiteCredito;

    // Getters y setters...
}
```

#### Esquema SQL Generado (DDL)
```sql
CREATE TABLE CuentaDebito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fechaCreacion TIMESTAMP,
    comisionDescubierto NUMERIC(19, 2)
);

CREATE TABLE CuentaCredito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fechaCreacion TIMESTAMP,
    limiteCredito NUMERIC(19, 2)
);
```

### Ventajas e Inconvenientes
* **Ventajas:** Excelente para reutilizar código común (atributos como `id`, `fechaCreacion`, `@Version` o auditoría) sin sobrecarga de joins ni tablas intermedias.
* **Inconvenientes:** No permite realizar consultas JPQL polimórficas como `SELECT b FROM BaseEntity b` ni establecer asociaciones `@ManyToOne` hacia la superclase.

---

## 2. `InheritanceType.SINGLE_TABLE` (Tabla Única por Jerarquía)

### ¿En qué consiste?
Esta es la estrategia **por defecto en JPA** [5]. Todas las clases de la jerarquía se materializan en **una única tabla física de la base de datos** [3, 5].
Para diferenciar a qué clase concreta pertenece cada fila, la tabla incluye una **columna discriminadora** (denominada por defecto `DTYPE`) [5, 6].

### Ejemplo Completo de Código

#### Clase Raíz (`Cuenta.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Estrategia de tabla única
@DiscriminatorColumn(name = "tipo_cuenta", discriminatorType = DiscriminatorType.STRING) // Nombre de columna discriminadora
public abstract class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titular;
    private BigDecimal saldo;

    // Getters y setters...
}
```

#### Subclases Concretas (`DebitAccount.java` y `CreditAccount.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DEBITO") // Valor guardado en la columna 'tipo_cuenta'
public class DebitAccount extends Cuenta {

    private BigDecimal comisionDescubierto;

    // Getters y setters...
}

@Entity
@DiscriminatorValue("CREDITO") // Valor guardado en la columna 'tipo_cuenta'
public class CreditAccount extends Cuenta {

    private BigDecimal limiteCredito;

    // Getters y setters...
}
```

#### Esquema SQL Generado (DDL) y Consulta Polimórfica
```sql
-- DDL
CREATE TABLE Cuenta (
    tipo_cuenta VARCHAR(31) NOT NULL, -- Columna discriminadora
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titular VARCHAR(255),
    saldo NUMERIC(19, 2),
    comisionDescubierto NUMERIC(19, 2), -- Nulable para CreditAccount
    limiteCredito NUMERIC(19, 2)         -- Nulable para DebitAccount
);
```

Al ejecutar una consulta polimórfica en JPQL (`SELECT c FROM Cuenta c`):
```sql
-- Consulta SQL limpia sin JOINs
SELECT c.id, c.titular, c.saldo, c.comisionDescubierto, c.limiteCredito, c.tipo_cuenta 
FROM Cuenta c;
```

### Ventajas e Inconvenientes
* **Ventajas:** **Rendimiento óptimo** en lectura y escritura, ya que las consultas polimórficas no requieren `JOIN` ni `UNION` [7, 8].
* **Inconvenientes:** **Pérdida de restricciones `NOT NULL`** en las columnas propias de las subclases (deben permitir valores nulos porque las filas de otras subclases no las usan) [7, 8].

---

## 3. `InheritanceType.JOINED` (Tabla por Subclase)

### ¿En qué consiste?
Cada clase de la jerarquía (tanto la superclase como cada subclase) tiene **su propia tabla independiente en la base de datos** [3, 9].
La tabla de la superclase almacena los atributos comunes, mientras que las tablas hijas almacenan únicamente sus campos específicos y una clave primaria que actúa simultáneamente como clave foránea hacia la tabla padre (`@PrimaryKeyJoinColumn`) [9, 10].

### Ejemplo Completo de Código

#### Clase Raíz (`Vehiculo.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Cada clase tiene su tabla
public abstract class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;
    private String modelo;

    // Getters y setters...
}
```

#### Subclases Concretas (`Coche.java` y `Moto.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "coches")
@PrimaryKeyJoinColumn(name = "vehiculo_id") // Opcional: personaliza la FK/PK
public class Coche extends Vehiculo {

    private Integer numeroPuertas;

    // Getters y setters...
}

@Entity
@Table(name = "motos")
@PrimaryKeyJoinColumn(name = "vehiculo_id")
public class Moto extends Vehiculo {

    private Boolean tieneSidecar;

    // Getters y setters...
}
```

#### Esquema SQL Generado (DDL) y Consulta Polimórfica
```sql
CREATE TABLE Vehiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(255),
    modelo VARCHAR(255)
);

CREATE TABLE coches (
    numeroPuertas INT,
    vehiculo_id BIGINT PRIMARY KEY,
    FOREIGN KEY (vehiculo_id) REFERENCES Vehiculo(id)
);

CREATE TABLE motos (
    tieneSidecar BOOLEAN,
    vehiculo_id BIGINT PRIMARY KEY,
    FOREIGN KEY (vehiculo_id) REFERENCES Vehiculo(id)
);
```

Al consultar `SELECT v FROM Vehiculo v`, Hibernate realiza `LEFT OUTER JOIN`s con todas las tablas hijas [11]:
```sql
SELECT v.id, v.marca, v.modelo, c.numeroPuertas, m.tieneSidecar,
       CASE 
           WHEN c.vehiculo_id IS NOT NULL THEN 1 
           WHEN m.vehiculo_id IS NOT NULL THEN 2 
           ELSE 0 
       END AS clazz_
FROM Vehiculo v
LEFT OUTER JOIN coches c ON v.id = c.vehiculo_id
LEFT OUTER JOIN motos m ON v.id = m.vehiculo_id;
```

### Ventajas e Inconvenientes
* **Ventajas:** **Máxima normalización relacional** y pleno soporte para restricciones de integridad (`NOT NULL`, `CHECK`, etc.) en cualquier columna [9, 12].
* **Inconvenientes:** Menor rendimiento en consultas polimórficas masivas debido a la proliferación de sentencias `OUTER JOIN` [11, 12].

---

## 4. `InheritanceType.TABLE_PER_CLASS` (Tabla por Clase Concreta)

### ¿En qué consiste?
Cada subclase concreta tiene **su propia tabla independiente conteniendo TODOS los atributos**, tanto los suyos propios como los heredados de la superclase [3, 13]. 
No existe una tabla separada para la superclase si esta es abstracta, o si existe, solo guarda instancias directas de ella [13, 14].

> **Nota técnica:** A diferencia de `JOINED`, aquí los atributos heredados se duplican físicamente en cada tabla relacional [13].

### Ejemplo Completo de Código

#### Clase Raíz (`Pago.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Nota: IDENTITY no es compatible en TABLE_PER_CLASS
    private Long id;

    private BigDecimal importe;

    // Getters y setters...
}
```

#### Subclases Concretas (`PagoTarjeta.java` y `PagoTransferencia.java`):
```java
package com.ejemplo.modelo;

import jakarta.persistence.Entity;

@Entity
public class PagoTarjeta extends Pago {

    private String numeroTarjeta;

    // Getters y setters...
}

@Entity
public class PagoTransferencia extends Pago {

    private String ibanOrigen;

    // Getters y setters...
}
```

#### Esquema SQL Generado (DDL) y Consulta Polimórfica
```sql
CREATE TABLE PagoTarjeta (
    id BIGINT PRIMARY KEY,
    importe NUMERIC(19, 2), -- Heredado de Pago
    numeroTarjeta VARCHAR(255)
);

CREATE TABLE PagoTransferencia (
    id BIGINT PRIMARY KEY,
    importe NUMERIC(19, 2), -- Heredado de Pago
    ibanOrigen VARCHAR(255)
);
```

Al ejecutar `SELECT p FROM Pago p`, Hibernate debe combinar los resultados de todas las tablas mediante sentencias `UNION` [15]:
```sql
SELECT p.id, p.importe, p.numeroTarjeta, p.ibanOrigen, p.clazz_ 
FROM (
    SELECT id, importe, numeroTarjeta, CAST(NULL AS VARCHAR) AS ibanOrigen, 1 AS clazz_ FROM PagoTarjeta
    UNION ALL
    SELECT id, importe, CAST(NULL AS VARCHAR) AS numeroTarjeta, ibanOrigen, 2 AS clazz_ FROM PagoTransferencia
) p;
```

### Ventajas e Inconvenientes
* **Ventajas:** Cada tabla contiene los datos completos de una entidad concreta y permite aplicar restricciones `NOT NULL`.
* **Inconvenientes:** **Muy desaconsejado por Hibernate** debido al alto impacto de rendimiento que provocan las consultas polimórficas con `UNION` [12, 15]. Además, limita las estrategias de generación de ID (como `IDENTITY`).

---

## 5. Uso con Repositorios en Spring Data JPA

Spring Data JPA soporta la herencia polimórfica de manera transparente. Puedes crear repositorios específicos para cada subclase o un repositorio polimórfico para la superclase:

```java
package com.ejemplo.repositorio;

import com.ejemplo.modelo.Cuenta;
import com.ejemplo.modelo.DebitAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Repositorio Polimórfico para la clase base
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // Recupera todas las cuentas (tanto débito como crédito)
    List<Cuenta> findByTitular(String titular);
}

// Repositorio Específico para una subclase
public interface DebitAccountRepository extends JpaRepository<DebitAccount, Long> {

    // Solo busca sobre cuentas de débito
    List<DebitAccount> findByComisionDescubiertoGreaterThan(Double comision);
}
```

---

## 6. Guía de Decisión: ¿Qué Estrategia Elegir?

Para tomar la decisión adecuada en tu proyecto, consulta esta matriz de recomendación basada en las guías oficiales de Hibernate:

| Criterio / Necesidad | Estrategia Recomendada | Razón Principal |
| :--- | :--- | :--- |
| **Reutilizar solo campos comunes (sin polimorfismo)** | **`@MappedSuperclass`** | No crea tablas innecesarias y mantiene el código Java limpio [1, 4]. |
| **Alto rendimiento en lecturas polimórficas** | **`SINGLE_TABLE`** | La lectura no requiere ningún `JOIN` ni `UNION`, siendo la más veloz [7, 8]. |
| **Estricta integridad de datos (`NOT NULL` en subclases)** | **`JOINED`** | Permite definir esquemas SQL limpios y normalizados con restricciones verdaderas [9, 12]. |
| **Consultas polimórficas frecuentes sobre colecciones grandes** | **Evitar `TABLE_PER_CLASS`** | Las consultas polimórficas con `UNION` escalan muy mal en bases de datos [12, 15]. |

---
💡 *¿Te gustaría añadir un ejercicio práctico o un repositorio funcional de ejemplo para probar estas cuatro estrategias con un test de rendimiento?*