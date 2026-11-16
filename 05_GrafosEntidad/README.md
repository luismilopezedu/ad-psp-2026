# 05_GrafosEntidad

Continuación de **04_ConsultasJPQL**, preparada para el tutorial de grafos de entidad.
Se conservan sus consultas, DTO, datos originales y pruebas. Java 21 y Spring Boot
4.1.2 se mantienen tal como aparecen en el POM recibido.

## Ejecución

Con JDK 21 y acceso a Maven Central, desde esta carpeta:

```bash
./mvnw spring-boot:run
```

En Windows: `mvnw.cmd spring-boot:run`. Si el ZIP no conserva los permisos en
macOS/Linux: `chmod +x mvnw`.

La configuración predeterminada usa H2 en memoria, muestra el SQL y ejecuta
`GrafosEntidadDemo`. No requiere Docker ni expone endpoints HTTP.
Al terminar se muestra `FIN DE LA DEMOSTRACIÓN DE GRAFOS`.

Para ejecutar la demostración JPQL anterior por separado:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.demo.enabled=false --app.jpql-demo.enabled=true"
```

Para ejecutar las pruebas:

```bash
./mvnw test
```

Para usar PostgreSQL (opcional):

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
docker compose down
```

Se conserva el puerto 5433 y la base de prácticas `jpql`. El perfil admite
`DB_URL`, `DB_USER` y `DB_PASSWORD`. Ambos perfiles usan `create-drop`:
el esquema se recrea; utiliza exclusivamente una base de datos de prácticas.

## Correspondencia con el tutorial

| Apartado | Implementación |
| --- | --- |
| 2. Modelo ampliado | Direccion, Etiqueta y LineaPedido; asociaciones nuevas en Cliente, Producto y Pedido |
| 3.1. Grafo simple | ClienteRepository.findWithDireccionByEmail |
| 3.2. Rutas compuestas | PedidoRepository.findByEstado |
| 3.2. Método estándar | PedidoRepository.findById |
| 3.2. Grafo con @Query | PedidoRepository.buscarPedidosGrandesConEtiquetas |
| 4. Grafo con nombre | Pedido.completoConDetalles y sus tres subgrafos |
| 4. Consultas con nombre | findCompletoById y findCompletoByClienteId |
| 5. FETCH y LOAD | findCompletoById (FETCH explícito) y findCompletoLoadById |

Todos esos métodos se invocan en `src/main/java/com/ejemplo/demo/GrafosEntidadDemo.java`.
La demostración no tiene una transacción global: recorre únicamente las asociaciones
incluidas en cada grafo después de regresar del repositorio.

## Datos de ejemplo

El DataSeed mantiene 3 categorías, 16 productos, 4 clientes y 8 pedidos.
Añade 3 direcciones, 3 etiquetas y 10 líneas. Cada total coincide con la suma de
cantidad por precio unitario de sus líneas. Hay productos compartidos entre pedidos,
productos sin etiquetas y un producto con dos etiquetas.

Ejemplos especialmente útiles:

- Ana: `ana@ejemplo.com`, dirección en Sevilla y 3 pedidos.
- Pedido de Ana de 1280 €: portátil y teclado; el portátil tiene dos etiquetas.
- Umbral de 600 €: devuelve los pedidos de 1280 € y 700 €.
- Daniel: sin dirección ni pedidos, para comprobar la asociación opcional.

Se mantiene el patrón original: `@Component`, `@PostConstruct` y
`TransactionTemplate` para cargar los datos dentro de una transacción real.
Direcciones y líneas se persisten mediante cascada; solo Etiqueta necesita un
repositorio nuevo.

## Decisiones de continuidad y precisiones

- Se conserva BigDecimal en Producto.precio, frente al Double del fragmento del
  tutorial, para mantener la compatibilidad con el proyecto base.
- Se mantienen constructores y getters explícitos, sin introducir Lombok.
- Cliente es el propietario de la relación unidireccional con Direccion
  (`direccion_id`); el fragmento no declara una asociación inversa en Direccion.
- Todas las asociaciones están configuradas como LAZY. Por ello FETCH y LOAD
  no presentan diferencias observables en este modelo; para compararlas frente a
  una asociación EAGER habría que cambiar deliberadamente ese mapeo.
- Los grafos tampoco eliminan por sí solos las limitaciones de cargar varias
  colecciones bag. Aquí se conserva List para las líneas y Set para las etiquetas,
  como en el tutorial. Las consultas profundas pueden multiplicar las filas SQL.
- No se añaden controladores, servicios CRUD ni repositorios sin uso.

## Validación

Se incluyen 6 pruebas de grafos en GrafosEntidadTest y las 6 pruebas JPQL originales.
Comprueban asociaciones fuera de sesión, resultados vacíos, dirección opcional,
los dos modos de grafo, etiquetas, ausencia de líneas duplicadas en el pedido de
1280 € y coherencia de importes.

Se revisaron la correspondencia de los métodos, el XML del POM y la sintaxis Java.
La ejecución de Maven se intentó, pero este entorno no pudo resolver
`repo.maven.apache.org` y descargar el POM padre de Spring Boot 4.1.2.
Por tanto, la compilación con dependencias, las pruebas de integración y el arranque
quedan pendientes de ejecutarse en un equipo con acceso a Maven Central.
