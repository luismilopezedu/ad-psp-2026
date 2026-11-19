# Aula Abierta · Una API REST en cinco pasos

El centro ofrece cursos organizados por áreas. Secretaría necesita dar de alta y retirar cursos, el alumnado quiere buscar en el catálogo y el profesorado necesita consultar el programa completo de cada área. Durante cinco sesiones de 45 minutos construirás esas funciones en **el mismo repositorio**. El punto de partida de cada sesión es el último commit de la anterior. Al terminar, etiqueta cada entrega como `etapa-1`, …, `etapa-5`.

## Antes de empezar

Abre `base/pom.xml` en tu IDE con JDK 21, Maven 3.6.3 o superior y procesamiento de anotaciones habilitado. Desde `base/`, arranca con `mvn spring-boot:run`. Usa `http://localhost:8080` y envía JSON con `Content-Type: application/json`. Recibes clases Java con atributos, DTO, un fichero de datos y los controladores de las tres primeras sesiones con respuestas provisionales 501. Puedes sustituir sus tipos de retorno por tipos concretos. A partir de la cuarta sesión tú diseñas también los controladores.

La base H2 en memoria se regenera al arrancar. Activa el juego de datos al completar el mapeo de la primera sesión. Usa estos valores en las pruebas; los identificadores son cadenas que asigna el centro.

| Curso | Título | Nivel | Horas | Área | Lecciones (id) |
|---|---|---|---:|---|---|
| C1 | Java inicial | BASICO | 10 | A · Tecnología | L1 Variables, L2 Práctica |
| C2 | Java web | MEDIO | 20 | A · Tecnología | L3 HTTP |
| C3 | Datos con Java | AVANZADO | 30 | A · Tecnología | L4 Práctica |
| C4 | Color inicial | BASICO | 25 | B · Diseño | L5 Paletas |
| C5 | Diseño web | MEDIO | 12 | B · Diseño | — |
| C6 | Inglés oral | AVANZADO | 8 | C · Idiomas | — |

Ordena por id ascendente las listas de cursos, lecciones y áreas. En cada entrega guarda en `pruebas.md` las peticiones utilizadas, el estado y un extracto del resultado. Entrega el repositorio con sus cinco etiquetas. Las tablas siguientes indican las pruebas mínimas y qué debe devolver cada operación.

## 1 · Altas y bajas en secretaría — 45 min

1. Convierte los tres POJO en entidades con Lombok y constructor sin argumentos. Un área ofrece varios cursos y cada curso pertenece a un área. Modela ambos sentidos y añade métodos para asignar y retirar un curso manteniendo ambos extremos coherentes. Un curso contiene cero o más lecciones propias. Para esta segunda relación basta navegar del curso a sus lecciones. Usa las columnas de `data.sql`, con claves externas en `curso.area_id` y `leccion.curso_id`. Excluye las asociaciones de métodos generados que puedan recorrerlas. Usa colecciones de tipo conjunto para poder cargar el programa completo más adelante.
2. Crea los repositorios necesarios con `JpaRepository`. Implementa el alta, la consulta y la retirada desde un servicio. Al retirar un curso, conserva físicamente su fila con `@SoftDelete` en la columna booleana `retirado`; elimina físicamente sus lecciones mediante cascada. El área se comparte y debe conservarse. El alta recibe un id nuevo y un área existente.
3. Cambia `spring.sql.init.mode` a `always`, reinicia y ejecuta estas pruebas en orden. Para verificar el borrado físico, llama a `GET /diagnostico/c1` (200) en el perfil predeterminado o local. Esta sonda facilitada lee las tres tablas por SQL.

| Método y ruta | Entrada de prueba | Respuesta esperada |
|---|---|---|
| POST /cursos | `{"id":"C7","titulo":"Git básico","nivel":"BASICO","horas":6,"areaId":"A"}` | 201, resumen con id, titulo y horas de C7 |
| GET /cursos/{id} | C7; después prueba X | 200, mismo resumen; X devuelve 404 |
| DELETE /cursos/{id} | C1 | 204 sin cuerpo. Repite GET para C1 → 404. Diagnóstico de C1 → retirado=true, lecciones=0, areaExiste=true |

**Entrega:** mapeos, helpers, repositorios, servicio, controlador y evidencia de la baja. Reinicia antes de empezar la sesión 2 para recuperar C1 y sus dos lecciones.

## 2 · El catálogo que consulta el alumnado — 45 min

1. Amplía el repositorio de cursos. Secretaría solicita un filtro por nivel exacto, una búsqueda por fragmento de título sin distinguir mayúsculas y un listado prioritario. En este último entra cualquier curso del nivel preferente; del nivel alternativo solo entran los que alcanzan el mínimo de horas. Expresa esa regla con un método derivado que combine And y Or.
2. Para imprimir fichas breves de varios cursos, selecciona los ids con IN y proyecta directamente al record `ResumenCurso` con un constructor JPQL. Conecta las cuatro operaciones al controlador facilitado.

| Método y ruta | Parámetros | Respuesta 200 |
|---|---|---|
| GET /catalogo/nivel | nivel=MEDIO | ["C2","C5"] |
| GET /catalogo/titulo | texto=jAvA | ["C1","C2","C3"] |
| GET /catalogo/prioridad | preferente=BASICO&alternativo=MEDIO&minimo=20 | ["C1","C2","C4"]. C1 entra con 10 h; C5 queda fuera con 12 h |
| GET /catalogo/resumen | ids=C4,C1,X | Dos objetos ordenados, C1 con 10 h y C4 con 25 h; solo id, titulo y horas |

**Entrega:** identifica en el commit la consulta que construye el DTO. Adjunta también una búsqueda de título sin coincidencias (200 y lista vacía).

## 3 · El programa de un curso y el de un área — 45 min

1. Mantén desactivado Open Session in View. Anota en dos líneas el fetch predeterminado de las asociaciones a uno y a muchos de tu modelo. Decide si conviene cargar el área por defecto al leer un curso y justifica tu configuración. Sitúa la conversión al DTO dentro de un servicio transaccional de lectura.
2. Implementa tres planes para obtener el detalle de un curso. El plan `join` usa JPQL con LEFT JOIN FETCH para incluir cursos sin lecciones. El plan `derivado` usa un método derivado y el plan `declarado` una consulta con @Query. En estos dos últimos aplica un grafo ad hoc para área y lecciones. Los tres comparten la conversión a `Vistas.Detalle`.
3. En Área define un grafo nombrado que cargue cursos y, mediante un subgrafo, sus lecciones. Reutilízalo al redeclarar el método heredado `findById` y en un método derivado de búsqueda por nombre exacto. Devuelve `Vistas.Programa`. Cada curso anidado tiene la misma forma que el detalle individual. Conserva el curso aunque tenga cero lecciones.

| Método y ruta | Entrada | Respuesta esperada |
|---|---|---|
| GET /lectura/curso/{id} | C1 con plan=join, derivado y declarado en tres peticiones | 200 en las tres; {"id":"C1","areaId":"A","lecciones":["L1","L2"]}. C5 con join → 200 y lista vacía |
| GET /lectura/area/{id} | A; después X | 200, programa con C1, C2 y C3 y sus lecciones según la tabla inicial; X → 404 |
| GET /lectura/programas | nombre=Diseño | 200, lista de un programa B con C4 (L5) y C5 (lista vacía) |

**Entrega:** adjunta la traza SQL de una petición de detalle y de una de programa. Señala qué asociaciones se leen dentro de la transacción y cómo se distingue el subgrafo en el código.

## 4 · Dos entornos y listados por páginas — 45 min

1. Conserva las propiedades comunes y crea `application-local.properties` y `application-demo.properties`. Configura local con H2 en memoria y demo con Postgresql desplegado desde un fichero `docker-compose.yml`. Además, la URL de conexión a la base de datos, el usuario y contraseña se deben leer de `AULA_URL`, `AULA_USER` y `AULA_PASS`. Define dos beans con la misma interfaz, seleccionados con @Profile, que devuelvan respectivamente LOCAL y DEMO. El endpoint de entorno debe utilizar el bean activo.
2. Crea un listado paginado por el id del área usando navegación por propiedades en un método derivado. Para el segundo listado escribe una subconsulta EXISTS correlacionada que compruebe si el curso tiene al menos una lección cuyo título coincide exactamente. Aplica la paginación en el repositorio. Usa `Vistas.Pagina` con ids, pagina, totalPaginas y totalElementos.
3. Ambos listados reciben `page` desde 0 y `size` de 1 a 50; valores por defecto 0 y 2. Orden fijo por id ascendente. Las páginas posteriores a la última dan 200 con ids vacío. Los parámetros numéricos inválidos dan 400. Reutiliza esta validación en la sesión 5.

| Método y ruta | Entrada | Respuesta esperada |
|---|---|---|
| GET /entorno | sin parámetros | 200, {"modo":"LOCAL"} o {"modo":"DEMO"} según el arranque |
| GET /paginas/cursos | area=A&page=1&size=2 | 200, {"ids":["C3"],"pagina":1,"totalPaginas":2,"totalElementos":3} |
| GET /paginas/con-leccion | titulo=Práctica&page=0&size=1 | 200, primera página C1, segunda C3, total 2. Con titulo=Ausente → total 0 |

**Entrega:** arranca sucesivamente con `SPRING_PROFILES_ACTIVE=local` y `SPRING_PROFILES_ACTIVE=demo`, desde el IDE o la terminal. En demo asigna a la URL `jdbc:h2:mem:demo`, define usuario y contraseña de prueba en el entorno y exclúyelos del repositorio. Registra la respuesta de /entorno en ambos arranques y una prueba con size=0.

## 5 · Una búsqueda avanzada por tres vías — 45 min

1. Amplía el repositorio con `JpaSpecificationExecutor`. Construye filtros opcionales con `PredicateSpecification<Curso>` para nivel y duración mínima, componiéndolos con AND. Varios niveles representan alternativas. Con cero filtros se devuelven todos los cursos visibles. Aplica la paginación mediante `findBy(predicado, q -> q.page(pageable))`.
2. Ofrece tres entradas que compartan el mismo servicio y el mismo DTO de salida paginado. La rápida recibe parámetros individuales y admite un solo nivel. La avanzada recibe la consulta como DTO JSON. La flexible recoge parámetros con `MultiValueMap<String,String>` y conserva todos los valores de `nivel`. En las tres, page y size viajan en la URL con las reglas de la etapa 4.
3. Acepta solo BASICO, MEDIO y AVANZADO y horas enteras ≥ 0. Omite filtros ausentes; en JSON, una lista vacía también significa sin filtro. Devuelve 400 para niveles desconocidos o vacíos, duración inválida y escalares repetidos en la URL. En la ruta flexible rechaza además claves desconocidas. En la rápida rechaza el nivel repetido. Los niveles repetidos en la flexible son válidos.

| Método y ruta | Entrada | Respuesta esperada |
|---|---|---|
| GET /busqueda/rapida | nivel=MEDIO&minHoras=15 | 200, ids=["C2"], totalElementos=1 |
| POST /busqueda/avanzada | cuerpo {"nivel":["BASICO","MEDIO"],"minHoras":15} | 200, ids=["C2","C4"], totalElementos=2 |
| GET /busqueda/flexible | nivel=BASICO&nivel=MEDIO&minHoras=15&size=1&page=1 | 200, ids=["C4"], totalPaginas=2, totalElementos=2 |

**Entrega final:** además de las tres pruebas, compara las tres vías con el mismo filtro MEDIO y mínimo 15. Comprueba la búsqueda sin filtros (6 cursos, 3 páginas por defecto), `minHoras=-1`, `nivel=OTRO` y `minHoras=5&minHoras=10`. Ejecuta otra vez el alta y la baja de la primera sesión; verifica que C1 desaparece tanto del catálogo como de la búsqueda avanzada.
