# Consultas JPQL con Spring Data JPA

Proyecto ejecutable para **todos los bloques de código** de `Tutorial-Consultas JPQL Spring.md`, incluido el BONUS. Aplicación de consola: al arrancar crea los datos, ejecuta las consultas, imprime sus resultados y termina. No tiene endpoints HTTP.

## Inicio rápido

Requisitos: **JDK 21 o superior** (también Java 25). La primera ejecución necesita Internet para descargar Maven y las dependencias. Importa `pom.xml` como proyecto Maven en IntelliJ IDEA y selecciona el mismo JDK para el proyecto y para Maven.

macOS / Linux, desde la carpeta descomprimida:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

También puedes ejecutar `com.ejemplo.Application` desde el IDE. Busca `=== FIN DE LA DEMOSTRACIÓN JPQL ===` en la consola. Después el proceso finaliza normalmente; no hay servidor web. H2 funciona en memoria y se reinicia en cada ejecución.

## Estructura

| Ruta dentro de `src/main/java/com/ejemplo` | Contenido |
|---|---|
| `modelo/` | Categoria, Producto, Cliente y Pedido; constructores y getters explícitos |
| `repositorio/` | Cuatro repositorios; consultas derivadas y JPQL del tutorial |
| `dto/ResumenPedidoDTO.java` | DTO con constructores de tres y cuatro argumentos |
| `config/DataSeed.java` | Carga de datos con `@Component` y `@PostConstruct` |
| `demo/ConsultasDemo.java` | Invocaciones etiquetadas por apartado, parámetros y salida por consola |
| `Application.java` | Punto de entrada y escaneo de componentes |

`src/test/java/com/ejemplo/ConsultasJpqlTest.java` contiene las comprobaciones de integración. No necesitas conocer JUnit para usar la práctica: la demostración de consola sirve como punto de partida.

## Correspondencia con el tutorial

| Apartado | Métodos ejecutados |
|---|---|
| 2 / Ejemplo 1 | `findByEmailAndActivoTrue`, `buscarPorEmailActivo`, `buscarActivosPorNombreParcial` |
| 3 / Ejemplo 2 | `findByCategoria_Nombre`, `buscarPorNombreCategoria`, `buscarProductosConCategoriaCargada` |
| 4 / Ejemplo 3 | `buscarPorRangoFechasYEstados` |
| 5 / Ejemplo 4 | `buscarPorFiltroTextoYPrecio` |
| 6 / Ejemplo 5 | `buscarPorCategoriaPaginado`, orden por precio |
| 7 | `obtenerResumenesPorEstado`, constructor DTO de tres argumentos |
| BONUS 1 | `buscarClienteActivoPorEmail` |
| BONUS 2 | `buscarPorNombreContiene` |
| BONUS 3 | `buscarPorNombreCategoria` |
| BONUS 4 | `buscarPedidosConClienteCargado` |
| BONUS 5 | `buscarPorEstados` |
| BONUS 6 | `buscarPedidosEnRangoYTotal` |
| BONUS 7 | `buscarPorPrecioMaximoSeguro` |
| BONUS 8 | `buscarClientesConPedidosMayoresA` |
| BONUS 9 | `buscarPorCategoriaPaginado`, orden por nombre |
| BONUS 10 | `obtenerResumenesConFechaPorEstado`, constructor DTO de cuatro argumentos |

Los métodos repetidos del tutorial se declaran una sola vez en cada repositorio. `MEMBER OF`, `ROUND`, `EXTRACT` y otras funciones solo mencionadas en el texto no tienen bloques ejecutables en el documento: no se añaden asociaciones artificiales para ellas.

## Datos de ejemplo y resultados esperados

Datos fijos, independientes de la fecha del sistema:

- **3 categorías:** Informática, Hogar y Sin productos.
- **16 productos:** 12 de Informática y 4 de Hogar. Incluyen un precio nulo y un precio cero.
- **4 clientes:** Ana, Bruno y Daniel activos; Carla inactiva. Daniel no tiene pedidos.
- **8 pedidos:** pendientes, en proceso, enviados y cancelados; fechas dentro y fuera de enero de 2026.

| Consulta / parámetros usados en la demostración | Resultado esperado |
|---|---|
| Email activo `ana@ejemplo.com` | Ana López |
| Email activo `carla@ejemplo.com` | Optional vacío |
| Nombre parcial `Ana` | Ana López |
| Productos de Informática | 12 |
| Productos con categoría cargada, precio >= 100 | 5 |
| Texto `TECLADO`, precio > 50 | Teclado mecánico |
| Enero + estados PENDIENTE/EN_PROCESO | 3 pedidos |
| Estados PENDIENTE/EN_PROCESO, sin fechas | 4 pedidos |
| Pedidos pendientes con cliente cargado | 3 pedidos |
| Enero + total >= 300 | 3 pedidos, incluidos los dos extremos del rango |
| Precio máximo seguro 0 | Producto sin precio y Muestra gratuita |
| Clientes con algún pedido > 600 | Ana y Carla |
| Página 0, tamaño 10, Informática | 10 elementos; total 12; 2 páginas |
| Página 1 por precio ascendente | Monitor 27 y Portátil Pro |
| Resúmenes ENVIADO | 3 DTO en cada variante |

Salvo en la paginación con `Sort`, las consultas no definen orden; compara conjuntos de resultados. La subconsulta EXISTS no filtra por cliente activo: por eso puede devolver a Carla.

## Decisiones de implementación

1. Se conserva **Spring Boot 4.1.2** y el Maven Wrapper de la base. Se baja el mínimo Java de 25 a **21** para facilitar su uso; se puede ejecutar con JDK 25. Se eliminan Web MVC y Lombok porque la aplicación no los necesita. Se añade H2; se mantiene el driver PostgreSQL.
2. Todos los importes son **BigDecimal** (`precision=12, scale=2`). Usa `new BigDecimal("80.00")` para crearlos. Los dos métodos que recibían Double incluyen sobrecargas de compatibilidad, de modo que `buscarProductosConCategoriaCargada(100.0)` sigue funcionando. La API principal usa BigDecimal; las sobrecargas rechazan null. No llames a una sobrecarga con un literal null.
3. Las dos versiones del DTO se reúnen en una clase con dos constructores. La variante de tres argumentos deja `fecha=null`. Se mantiene `obtenerResumenesPorEstado` para ella; la del BONUS se llama `obtenerResumenesConFechaPorEstado` para evitar dos métodos Java con idéntica firma.
4. `Producto.categoria` y `Pedido.cliente` son ManyToOne **LAZY**, obligatorias y sin cascadas. Se guardan primero categorías y clientes. Las asociaciones son unidireccionales, suficientes para todas las consultas. No se inventa una asociación Pedido–Producto: los totales se cargan directamente como en el modelo del tutorial.
5. `DataSeed` usa **TransactionTemplate** en `@PostConstruct`. Una anotación `@Transactional` en ese mismo método no es una forma fiable de activar la transacción durante la inicialización. La carga se confirma antes de ejecutar `ConsultasDemo`, mediante `@DependsOn("dataSeed")`.
6. La demostración no tiene una transacción global. Solo recorre asociaciones cargadas con FETCH; los `toString()` de las entidades no recorren asociaciones LAZY. Esto permite observar la diferencia entre JOIN y JOIN FETCH sin que una sesión global la oculte.
7. Se busca la categoría por nombre antes de paginar, evitando depender de IDs fijos. Los IDs escritos literalmente en el tutorial pueden sustituirse por el ID devuelto por `CategoriaRepository`.
8. Para IN, usa listas no vacías. El comportamiento de una colección vacía puede variar entre proveedores; si tu aplicación lo necesita, resuélvelo en el servicio devolviendo una lista vacía.

## PostgreSQL opcional

El perfil es **solo para la base de datos de práctica**: `create-drop` recrea y elimina las tablas al arrancar/cerrar. No lo apuntes a una base con datos que quieras conservar.

```bash
docker compose up -d --wait
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Usa PostgreSQL 17 en `localhost:5433`, base/usuario/contraseña `jpql`. Son credenciales locales de ejemplo. Se pueden sustituir mediante `DB_URL`, `DB_USER` y `DB_PASSWORD`. No es necesario Docker para la ejecución normal con H2.

```bash
docker compose down
```

## Modificar, probar y empaquetar

Cambia parámetros en `ConsultasDemo.ejecutar()` y vuelve a arrancar. Cambia los datos en `DataSeed` para ensayar otros casos. Puedes desactivar la demostración con `--app.demo.enabled=false` (se sigue cargando el seed).

```bash
./mvnw test
./mvnw clean package
java -jar target/tutorial-jpql-1.0.0.jar
```

Para ver los valores enlazados a los parámetros además del SQL, añade a `application.properties`:

```properties
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

Documentación de referencia: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
