# 06_PerfilesEntornos

Continuación de 05_GrafosEntidad para practicar perfiles y externalización de
configuración. Conserva las siete entidades, repositorios, consultas JPQL, grafos
y datos de la práctica anterior. Ambos entornos usan PostgreSQL 17; se elimina H2.

## Requisitos

- JDK 21.
- Docker con Docker Compose v2 (por ejemplo, Docker Desktop en macOS).
- Acceso a Maven Central para descargar las dependencias.
- Puertos locales 5433 y 5434 libres.

Se mantienen Java 21 y Spring Boot 4.1.2 del proyecto adjunto. El Maven Wrapper
está incluido. Los comandos siguientes se ejecutan desde la raíz del proyecto,
en Terminal de macOS/Linux. Si es necesario, ejecutar primero `chmod +x mvnw`.
En Windows se utiliza `mvnw.cmd` y se definen las variables en el IDE o PowerShell.

## 1. Preparar y levantar las dos bases de datos

```bash
cp .env.example .env
```

Editar la contraseña de ejemplo en .env antes de continuar. Es una simulación
local de producción. .env está excluido de Git.

```bash
docker compose up -d --wait
docker compose ps
```

También se puede levantar únicamente un servicio:

```bash
docker compose up -d --wait postgres-dev
docker compose up -d --wait postgres-prod
```

Es necesario preparar .env incluso al levantar solo dev: Compose interpola las
variables obligatorias declaradas en el archivo completo.

| Característica | dev | prod |
| --- | --- | --- |
| Servicio Compose | postgres-dev | postgres-prod |
| Base de datos | tienda_dev | tienda_prod |
| Puerto desde el ordenador | 5433 | 5434 |
| Almacenamiento | tmpfs, límite de 256 MiB | Volumen postgres_prod_data |
| DDL de Hibernate | create-drop | validate |
| Datos ficticios | Sí | No |
| SQL en consola | Sí | No |
| Demostración de grafos | Sí | No |
| Credenciales Spring | DEV_DB_USERNAME / DEV_DB_PASSWORD, con valores locales predeterminados | DB_USERNAME / DB_PASSWORD, obligatorias |

tmpfs almacena los datos de desarrollo en memoria del host Docker (en Docker
Desktop, su entorno Linux). Se pierden al detener el contenedor. El sistema puede
utilizar swap. Además, create-drop recrea las tablas al arrancar la aplicación y
las elimina al cerrar su contexto.

El volumen de prod sobrevive a detener o recrear el contenedor y a
`docker compose down`. Compose añade su prefijo de proyecto al nombre del volumen.

## 2. Ejecutar desarrollo

Con los valores locales predeterminados basta con:

```bash
./mvnw spring-boot:run
```

La configuración base establece `spring.profiles.default=dev`. Solo actúa cuando
no se ha indicado un perfil activo, y permite que @Profile seleccione dev.

Si se han cambiado credenciales o URLs en .env, exportarlas antes de arrancar:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Orden de ejecución:

1. DevDataInitializer carga los datos ficticios dentro de TransactionTemplate.
2. GrafosEntidadDemo ejecuta los ejemplos de grafos fuera de una transacción global.
3. ConsultasDemo puede ejecutar los ejemplos JPQL si se habilita.

Se mantienen 3 categorías, 16 productos, 4 clientes, 8 pedidos, 3 direcciones,
3 etiquetas y 10 líneas.

Para ejecutar solo la demostración JPQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.demo.enabled=false --app.jpql-demo.enabled=true"
```

## 3. Ejecutar producción

Compose lee .env automáticamente para interpolar su configuración.
Spring Boot ejecutado desde Maven o Java NO lee .env automáticamente.
Exportar sus valores en esa misma terminal:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Este perfil valida el esquema ya creado por PostgreSQL, utiliza el pool Hikari
del tutorial y no instancia el inicializador ni las demostraciones.

Es una aplicación de consola, no una API REST: no mantiene un servidor HTTP.
En prod es normal que arranque, valide el esquema y finalice sin imprimir ejemplos.
El proceso de PostgreSQL y sus datos siguen disponibles en Docker.

## 4. Esquema inicial y persistencia

`docker/postgres/prod/01-schema.sql` crea las ocho tablas (incluida la tabla de
unión producto_etiquetas), claves externas e identificadores identity.
No inserta datos de ejemplo.

La imagen oficial de PostgreSQL ejecuta ese SQL únicamente cuando inicializa
un directorio de datos vacío. Así prod puede usar validate desde el primer arranque.
Reiniciar con un volumen existente no vuelve a ejecutar el SQL. Cambios posteriores
del modelo requieren cambios explícitos de esquema; esta práctica no incorpora
Flyway ni Liquibase.

Para comprobar manualmente la persistencia con un dato de prueba:

```bash
docker compose exec postgres-prod sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

En la consola psql:

```sql
INSERT INTO categorias(nombre) VALUES ('Prueba de persistencia');
SELECT * FROM categorias;
\q
```

Después:

```bash
docker compose restart postgres-prod
docker compose exec postgres-prod sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT * FROM categorias;"'
```

El registro debe mantenerse. Esta inserción es una comprobación manual opcional:
la aplicación no carga datos ficticios en prod.

Para detener ambos servicios conservando el volumen:

```bash
docker compose down
```

No añadir `-v` si se quieren conservar los datos de prod: esa opción borra
el volumen. Cambiar DB_USERNAME o DB_PASSWORD en .env tampoco cambia un usuario
que ya exista dentro de un volumen inicializado.

## 5. Formas de activar los perfiles del tutorial

Para generar el JAR sin ejecutar las pruebas de integración:

```bash
./mvnw package -DskipTests
```

Con las variables de conexión exportadas, elegir UNA de estas alternativas:

**Argumento de aplicación:**

```bash
java -jar target/06_PerfilesEntornos-1.0.0.jar --spring.profiles.active=prod
```

**Propiedad JVM (antes de -jar):**

```bash
java -Dspring.profiles.active=prod -jar target/06_PerfilesEntornos-1.0.0.jar
```

**Variable de entorno:**

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/06_PerfilesEntornos-1.0.0.jar
```

**IntelliJ/Eclipse/VS Code:**

Ejecutar com.ejemplo.Application. Definir prod en Active Profiles o
`--spring.profiles.active=prod` en Program Arguments, y DB_USERNAME / DB_PASSWORD
en Environment Variables. DB_URL puede sobrescribirse para otra dirección.

Activar dev y prod de forma excluyente. La expresión `@Profile("dev & !prod")`
impide cargar datos ficticios cuando prod está activo, pero no sustituye una
selección coherente de los archivos de configuración.

## 6. Externalización y correspondencia con el tutorial

| Fragmento del tutorial | Archivo / implementación |
| --- | --- |
| Configuración común y selección predeterminada | src/main/resources/application.properties |
| PostgreSQL temporal, SQL visible y create-drop | application-dev.properties |
| Credenciales externas, Hikari y validate | application-prod.properties |
| @Profile y carga de datos de desarrollo | config/DevDataInitializer.java |
| Activación por CLI, JVM, entorno e IDE | Apartado 5 del README |
| Infraestructura de ambos entornos | docker-compose.yml y .env.example |

Las variables DB_URL y DEV_DB_URL permiten apuntar a otra instancia sin recompilar.
Los valores del ejemplo asumen que Java se ejecuta en el ordenador anfitrión;
no se ha añadido un contenedor de aplicación.

También se puede proporcionar un archivo externo de propiedades, por ejemplo:

```bash
java -jar target/06_PerfilesEntornos-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:/ruta/mi-configuracion.properties
```

Ese archivo puede contener spring.datasource.url, spring.datasource.username y
spring.datasource.password; las propiedades externas sobrescriben las empaquetadas.
No se incluyen credenciales reales.

Adaptaciones respecto al tutorial y al proyecto anterior:

- PostgreSQL sustituye a H2 también en dev; no hay consola H2.
- DevDataInitializer sustituye a DataSeed, reutilizando todos sus datos.
- Se utiliza CommandLineRunner, como en el tutorial, y @Order para ejecutar
  las demostraciones después de cargar los datos; se retiran sus @PostConstruct
  y @DependsOn anteriores.
- El inicializador y las demostraciones llevan @Profile("dev & !prod").
- Se mantienen los constructores y BigDecimal del modelo existente.
- spring.profiles.default expresa el perfil por defecto sin forzar uno activo.
- El Compose adjunto no declaraba volumen: se añade uno exclusivo para prod.
- No se añaden entidades, endpoints, servicios CRUD ni dependencias de migraciones.

## 7. Pruebas y verificación

Con postgres-dev disponible y las variables DEV_DB_* exportadas si se han cambiado:

```bash
./mvnw test
```

Las 12 pruebas de integración heredadas usan @ActiveProfiles("dev") y PostgreSQL,
con las demostraciones deshabilitadas. Recrean el esquema de tienda_dev;
no deben ejecutarse contra una base con datos que se quieran conservar.
PerfilesTest verifica, sin conexión a base de datos, que prod excluye el
inicializador y las demostraciones, incluso si también se activa dev.

Para ejecutar solo esa comprobación de selección de componentes:

```bash
./mvnw -Dtest=PerfilesTest test
```

Validación realizada al preparar esta entrega: sintaxis Java, XML del POM,
estructura YAML, coherencia de configuración y contenido del ZIP.
La compilación y las pruebas se intentaron con Maven, pero el entorno no pudo
resolver Maven Central. Tampoco dispone de Docker/PostgreSQL para comprobar
el arranque de los contenedores, el SQL y la validación Hibernate en ejecución.
Estas comprobaciones quedan pendientes en el equipo local.

## Referencias

- https://docs.spring.io/spring-boot/reference/features/profiles.html
- https://docs.spring.io/spring-boot/reference/features/external-config.html
- https://docs.docker.com/engine/storage/tmpfs/
- https://docs.docker.com/reference/compose-file/services/
- https://hub.docker.com/_/postgres
