# Tutorial Maestro: Gestión Multientorno en Spring Boot con Perfiles y Bases de Datos (H2 vs. PostgreSQL)

Me alegra acompañarte en este tutorial dedicado a uno de los pilares de la arquitectura de aplicaciones empresariales con **Spring Boot**: la **gestión de entornos mediante Perfiles (*Spring Profiles*)** [1].

---

## 1. El Reto: ¿Por qué necesitamos Perfiles?

En un desarrollo profesional de software, el código fuente atraviesa diferentes fases o entornos antes de llegar a los usuarios finales:

1. **Desarrollo / Local (`dev`):** Se ejecuta en la máquina personal del programador. Interesa usar una base de datos ligera, rápida y fácil de reiniciar, como **H2 en memoria** [2, 3].
2. **Pruebas / QA (`test`):** Entorno para ejecutar tests de integración o pruebas automatizadas con datos controlados.
3. **Producción (`prod`):** El entorno real donde viven los clientes. Se conecta a una base de datos robusta como **PostgreSQL**, con un pool de conexiones optimizado, seguridad estricta y datos persistentes.

El principio fundamental del diseño limpio es **no modificar una sola línea de código fuente Java** cuando pasamos de un entorno a otro. Spring Boot resuelve esto mediante el concepto de **Perfiles** [1].

---

## 2. Hilo Conductor: Dominio de Comercio Electrónico

Seguiremos utilizando nuestro modelo de dominio de **comercio electrónico**:

*   **`Categoria`**: (`id`, `nombre`)
*   **`Producto`**: (`id`, `nombre`, `precio`, `categoria` `@ManyToOne`)
*   **`Cliente`**: (`id`, `nombre`, `email`, `activo`)
*   **`Pedido`**: (`id`, `fecha`, `total`, `estado`, `cliente` `@ManyToOne`)

---

## 3. La Anotación `@Profile` en Código Java

La anotación **`@Profile`** permite condicionar la carga de beans de Spring (clases anotadas con `@Component`, `@Service`, `@Repository`, `@Configuration`, etc.) para que existan en el contenedor de Inyección de Dependencias **únicamente si determinado perfil está activo** [1, 4].

### Sintaxis y Posibilidades

*   `@Profile("dev")`: Se activa solo cuando el perfil `dev` está activo [1].
*   `@Profile("!prod")`: Se activa en **cualquier entorno excepto** en producción.
*   `@Profile({"dev", "test"})`: Se activa si está activo el perfil `dev` **o** el perfil `test`.

---

### Ejemplo Práctico: Carga de Datos Iniciales en Desarrollo (`DataLoader`)

En desarrollo (`dev`), resulta muy útil contar con categorías y productos ficticios insertados automáticamente al arrancar la aplicación para poder hacer pruebas en local. Sin embargo, **¡en producción jamás debemos ejecutar un script que inserte datos de prueba!**

Implementamos esta lógica restringiendo la clase con `@Profile("dev")`:

```java
package com.ejemplo.config;

import com.ejemplo.modelo.Categoria;
import com.ejemplo.modelo.Producto;
import com.ejemplo.repositorio.CategoriaRepository;
import com.ejemplo.repositorio.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev") // ⚠️ ESTE BEAN SOLO SE INSTANCIA SI EL PERFIL ACTIVO ES 'dev'
public class DevDataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public DevDataInitializer(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [Perfil DEV] Inicializando datos de prueba en la base de datos H2...");

        Categoria electronica = new Categoria();
        electronica.setNombre("Electrónica");
        categoriaRepository.save(electronica);

        Producto laptop = new Producto();
        laptop.setNombre("Portátil Gaming");
        laptop.setPrecio(1200.0);
        laptop.setCategoria(electronica);
        productoRepository.save(laptop);

        System.out.println(">>> [Perfil DEV] Datos ficticios cargados correctamente.");
    }
}
```

---

## 4. Configuración de Entornos con Archivos `properties`

Spring Boot utiliza una convención de nombres para cargar archivos de configuración específicos según el perfil activo [5, 6]:

\\[\text{application-}\{\text{perfil}\}\text{.properties}\\]

### Estructura en el Proyecto (`src/main/resources`)
```text
src/main/resources/
├── application.properties        <-- Configuración base (común a todos)
├── application-dev.properties    <-- Configuración específica de Desarrollo (H2)
└── application-prod.properties   <-- Configuración específica de Producción (PostgreSQL)
```

---

### 1. `application.properties` (Configuración Base Común)

Este archivo almacena la configuración compartida por cualquier entorno [7, 8]. Además, puede definir el perfil activo por defecto para el entorno local [9, 10].

```properties
# Nombre de la aplicación
spring.application.name=TiendaOnlineApp

# Perfil activo por defecto (si no se especifica otro al arrancar)
spring.profiles.active=dev

# Formato de dialecto y logs comunes
spring.jpa.open-in-view=false
```

---

### 2. `application-dev.properties` (Perfil de Desarrollo: H2 en Memoria)

En el entorno de desarrollo nos conectamos a una base de datos **H2 en memoria** [2, 3]. Las propiedades definidas en este archivo sobrescriben a las del archivo base `application.properties` cuando el perfil `dev` está activo [5, 6].

```properties
# -------------------------------------------------------------
# CONFIGURACIÓN DATASOURCE: H2 DATABASE (Memoria)
# -------------------------------------------------------------
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:tiendadb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=

# Habilitar la Consola Web de H2 en el navegador (http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# -------------------------------------------------------------
# CONFIGURACIÓN JPA / HIBERNATE PARA DESARROLLO
# -------------------------------------------------------------
# Recrea el esquema al arrancar y lo destruye al apagar
spring.jpa.hibernate.ddl-auto=create-drop

# Muestra las consultas SQL formateadas en la consola
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

### 3. `application-prod.properties` (Perfil de Producción: PostgreSQL)

En producción nos conectamos a una instancia física de **PostgreSQL**. Por motivos de seguridad e integridad de los datos, la estrategia de generación DDL cambia drásticamente [11, 12].

```properties
# -------------------------------------------------------------
# CONFIGURACIÓN DATASOURCE: POSTGRESQL (Producción)
# -------------------------------------------------------------
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Pool de conexiones HikariCP optimizado para producción
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# -------------------------------------------------------------
# CONFIGURACIÓN JPA / HIBERNATE PARA PRODUCCIÓN
# -------------------------------------------------------------
# ⚠️ NO DEBERÍAMOS usar create-drop ni update en producción. 
# 'validate' comprueba que las tablas coinciden con las entidades Java sin modificar nada.
spring.jpa.hibernate.ddl-auto=validate

# Ocultar SQL e impresiones pesadas en logs de producción por rendimiento y privacidad
spring.jpa.show-sql=false
```

> **Consejo de Seguridad:** En `application-prod.properties`, nota cómo la clave `${DB_USERNAME}` y la contraseña `${DB_PASSWORD}` hacen referencia a **variables de entorno del sistema** [13, 14]. Jamás debes subir credenciales reales de producción al repositorio de código (Git) [13, 15].

---

## 5. ¿Cómo activar los Perfiles en Tiempo de Ejecución?

Spring Boot ofrece múltiples formas de seleccionar el perfil activo [9, 13, 15]:

### Opción A: Desde la línea de comandos al ejecutar el archivo JAR
```bash
java -jar tienda-app.jar --spring.profiles.active=prod
```

### Opción B: Usando Parámetros de la Máquina Virtual Java (JVM)
```bash
java -Dspring.profiles.active=prod -jar tienda-app.jar
```

### Opción C: Mediante Variables de Entorno del Sistema (Ideal para Docker/Kubernetes)
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar tienda-app.jar
```

### Opción D: En el IDE (Eclipse / IntelliJ / VS Code)
En las configuraciones de ejecución (*Run/Debug Configurations*), puedes escribir `prod` o `dev` en la casilla **Active Profiles** o en **Program Arguments**.

---

## 6. Resumen Comparativo de Entornos

| Característica | Perfil `dev` (Desarrollo) | Perfil `prod` (Producción) |
| :--- | :--- | :--- |
| **Motor de BD** | H2 Database (En Memoria / Archivo) [2, 3] | PostgreSQL (Servidor Dedicado) |
| **Estrategia DDL (`ddl-auto`)** | `create-drop` o `update` [11, 12] | `validate` o `none` (vía Flyway/Liquibase) [11, 12] |
| **Consola H2** | Habilitada (`/h2-console`) | Deshabilitada |
| **Visualización SQL (`show-sql`)** | `true` (en consola) [2, 16] | `false` (logs limpios) |
| **Carga de Datos (`DataLoader`)** | Activa mediante `@Profile("dev")` | Inactiva / Omitida [1, 4] |
