# Arcade de barrio · Siete prácticas para construir una API REST

Una asociación organiza una muestra de videojuegos. Primero necesita inventariarlos; después quiere publicar el catálogo, consultar los logros de cada título y ofrecer búsquedas paginadas para su web. Trabajarás siempre en el mismo proyecto Maven. Al final de cada sesión conserva un commit con el número del ejercicio y los resultados de sus pruebas.

## Preparación previa

Importa `base/pom.xml` con Java 21, Maven 3.6.3+ y procesamiento de anotaciones Lombok activo. Con las dependencias descargadas, ejecuta `mvn spring-boot:run` desde `base/`. La URL base es `http://localhost:8080`. Los controladores apuntan a siete servicios con operaciones pendientes (HTTP 501). Puedes editar sus firmas. Recibes los DTO, las tablas y el seed. Hibernate utiliza el esquema SQL facilitado. En las escrituras usa un servicio transaccional. Devuelve DTO, ids o valores simples según el contrato. Ordena las listas de ids, fichas y elementos anidados por id ascendente.

**Al inicio de cada ejercicio reinicia la aplicación.** Así recuperas los datos iniciales y conservas todo el código anterior. Desde la carpeta que contiene los documentos ejecuta `python3 comprobar.py N` con el número de ejercicio. La prueba 1 crea J7 y la 2 retira J1. Reinicia antes de repetir cualquiera de las dos. El cliente usa únicamente la biblioteca estándar de Python. También puedes reproducir cada fila en Postman. Para los POST envía JSON con Content-Type application/json.

| Id | Título | Género | Precio (entero) | Plataforma | Logros |
|---|---|---|---:|---|---|
| J1 | Luna roja | PUZLE | 8 | PC · Ordenador | L1 Inicio, L2 Meta |
| J2 | Ruta lunar | CARRERAS | 20 | PC | — |
| J3 | Mareas | AVENTURA | 30 | PC | L3 Meta |
| J4 | Luna azul | PUZLE | 25 | SW · Portátil | L4 Inicio |
| J5 | Rally | CARRERAS | 12 | SW | — |
| J6 | Eco | AVENTURA | 5 | VR · Visor | — |

En las tablas, cada petición incluye un ejemplo de entrada y la respuesta esperada sobre esos datos. Las cadenas sueltas se pueden devolver como texto plano. Cada sesión comienza con la implementación de la anterior.

## 1. El inventario · 30 minutos

1. Completa Juego y Plataforma como entidades. Usa Lombok para accesores y constructor sin argumentos. Mapea Juego → Plataforma con la FK plataforma_id. Usa id asignado de tipo String. El esquema y la entidad Logro ya están preparados.
2. Crea los dos JpaRepository e inyéctalos en el servicio. Implementa las cinco operaciones con save, findAll, findById o count. El alta recibe un id nuevo y el id de una plataforma existente. Usa Ficha para los detalles y devuelve 404 si el recurso solicitado no existe.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `POST /juegos`<br>Cuerpo `{"id":"J7","titulo":"Bruma","genero":"PUZLE","precio":9,"plataforma":"PC"}` | 201 | `{"id":"J7","titulo":"Bruma","precio":9}` |
| `GET /juegos/J7` | 200 | `{"id":"J7","titulo":"Bruma","precio":9}` |
| `GET /juegos` | 200 | `["J1","J2","J3","J4","J5","J6","J7"]` |
| `GET /plataformas` | 200 | `["PC","SW","VR"]` |
| `GET /inventario/total` | 200 | `7` |

**Comprobación y entrega.** Comprueba GET /juegos/X → 404. Entrega el commit y el resultado del cliente.

## 2. Qué conserva el archivo · 30 minutos

1. Completa la asociación bidireccional Plataforma–Juego. Escribe un helper que permita asignar o cambiar la plataforma y mantenga las dos colecciones coherentes; úsalo también en el alta. Asocia cada juego con sus logros mediante una colección y la FK logro.juego_id. Usa conjuntos e identidad estable al modificar asociaciones.
2. Implementa la retirada con @SoftDelete sobre juego.retirado y borrado físico en cascada de sus logros. La plataforma compartida debe sobrevivir. Trabaja en un servicio transaccional. Para la auditoría de J1 delega en Inspeccion.comprobarJ1(), ya facilitado.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /juegos/J1/plataforma` | 200 | `"PC"` |
| `GET /juegos/J1/logros` | 200 | `["L1","L2"]` |
| `DELETE /juegos/J1` | 204 | Sin cuerpo |
| `GET /archivo/visibles` | 200 | `5` |
| `GET /archivo/auditoria-j1` | 200 | `{"retirado":true,"logrosOriginales":0,"plataformaExiste":true}` |

**Comprobación y entrega.** Después de la baja, GET /juegos/J1 → 404 y GET /juegos/J3/logros → ["L3"]. Muestra en el depurador cómo el helper cambia un juego de plataforma y actualiza ambos extremos.

## 3. El buscador del catálogo · 30 minutos

1. Implementa los tres primeros filtros en la base de datos. El título admite fragmentos sin distinguir mayúsculas. En prioridad entra cualquier juego del género preferente o uno del alternativo cuyo precio alcance el mínimo. Usa And y Or en el método derivado.
2. Resuelve la selección de varios ids con IN. Para la última operación proyecta directamente a Ficha por constructor JPQL los juegos del género solicitado.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /catalogo/genero?genero=CARRERAS` | 200 | `["J2","J5"]` |
| `GET /catalogo/titulo?texto=LuN` | 200 | `["J1","J2","J4"]` |
| `GET /catalogo/prioridad?preferente=PUZLE&alternativo=CARRERAS&minimo=20` | 200 | `["J1","J2","J4"]` |
| `GET /catalogo/seleccion?ids=J4,J1,X` | 200 | `["J1","J4"]` |
| `GET /catalogo/fichas?genero=PUZLE` | 200 | `[{"id":"J1","titulo":"Luna roja","precio":8},{"id":"J4","titulo":"Luna azul","precio":25}]` |

**Comprobación y entrega.** Busca texto=xyz y verifica 200 con []. Explica por qué el listado prioritario incluye J1 y excluye J5.

## 4. Una ficha sin consultas imprevistas · 30 minutos

1. Explica el fetch predeterminado de cada cardinalidad y justifica el que eliges para Juego.plataforma. Conserva OSIV desactivado y transforma entidades en Tipos.Detalle o Tipos.Programa dentro del servicio @Transactional(readOnly=true). Comparte la conversión entre las cinco rutas.
2. Para un juego, implementa LEFT JOIN FETCH de logros en la primera ruta, un grafo ad hoc en un método derivado en la segunda y un grafo ad hoc sobre un método @Query en la tercera. Conserva los juegos sin logros.
3. Para la plataforma, define un grafo nombrado con subgrafo juegos → logros. Reutilízalo en findById redeclarado con su firma heredada y en una consulta derivada por nombre exacto. Guarda una traza SQL y señala dónde termina la transacción.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /lectura/join/J2` | 200 | `{"id":"J2","logros":[]}` |
| `GET /lectura/derivada/J1` | 200 | `{"id":"J1","logros":["L1","L2"]}` |
| `GET /lectura/declarada/J1` | 200 | `{"id":"J1","logros":["L1","L2"]}` |
| `GET /programas/PC` | 200 | `{"id":"PC","juegos":[{"id":"J1","logros":["L1","L2"]},{"id":"J2","logros":[]},{"id":"J3","logros":["L3"]}]}` |
| `GET /programas?nombre=Ordenador` | 200 | `[{"id":"PC","juegos":[{"id":"J1","logros":["L1","L2"]},{"id":"J2","logros":[]},{"id":"J3","logros":["L3"]}]}]` |

**Comprobación y entrega.** Entrega las cinco respuestas, la traza de una ficha y las líneas del servicio que convierten a DTO dentro de la transacción.

## 5. El catálogo de demostración · 30 minutos

1. Crea application-local.properties y application-demo.properties. Conserva el esquema y el seed en ambos arranques. Usa H2 en memoria en los dos, con nombre arcadeLocal en local y una URL, usuario y clave leídos de ARCADE_URL, ARCADE_USER y ARCADE_PASS en demo.
2. Implementa dos beans Politica, activos mediante @Profile. Local publica LOCAL, EUR y un límite de página de 3. Demo publica DEMO, USD y un límite de 2. Inyecta la política y utiliza sus valores en los cinco endpoints. El límite es operativo desde la etapa 6.
3. Arranca sucesivamente usando SPRING_PROFILES_ACTIVE=local y demo desde la terminal o el IDE. Para demo usa jdbc:h2:mem:arcadeDemo y credenciales ficticias definidas en el entorno. Entrega las respuestas en ambos perfiles. Vuelve a local para las sesiones siguientes.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /config/modo` | 200 | `"LOCAL"` |
| `GET /config/moneda` | 200 | `"EUR"` |
| `GET /config/limite` | 200 | `3` |
| `GET /config/resumen` | 200 | `{"modo":"LOCAL","moneda":"EUR","limite":3}` |
| `GET /config/importe/J1` | 200 | `{"precio":8,"moneda":"EUR"}` |

**Comprobación y entrega.** La tabla corresponde a local. Ejecuta también python3 comprobar.py 5 demo tras arrancar en demo. Los precios numéricos son importes de demostración y conservan su valor en ambos perfiles.

## 6. Cinco listados paginados · 30 minutos

1. Construye un Pageable común a partir de page (desde cero) y size. Usa el límite del perfil y orden estable por id; rechaza con 400 páginas negativas o tamaños fuera de 1..límite. Una página posterior a la última devuelve 200 con ids vacío y los totales reales.
2. Pagina en la BD el catálogo completo, los juegos de plataforma (navega a plataforma.id en un método derivado), un género y un precio mínimo inclusivo. Para el último listado usa una subconsulta EXISTS correlacionada que localice juegos con al menos un logro de nombre exacto. Emplea Tipos.Pagina.de para las cinco respuestas.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /paginas/juegos?page=1&size=2` | 200 | `{"ids":["J3","J4"],"total":6,"paginas":3}` |
| `GET /paginas/plataforma?id=PC&page=1&size=2` | 200 | `{"ids":["J3"],"total":3,"paginas":2}` |
| `GET /paginas/genero?genero=PUZLE` | 200 | `{"ids":["J1","J4"],"total":2,"paginas":1}` |
| `GET /paginas/precio?minimo=20` | 200 | `{"ids":["J2","J3"],"total":3,"paginas":2}` |
| `GET /paginas/logro?nombre=Meta&page=1&size=1` | 200 | `{"ids":["J3"],"total":2,"paginas":2}` |

**Comprobación y entrega.** Comprueba nombre=Ausente en el listado por logro (total 0), size=0 (400) y page=10 (200 con ids vacío).

## 7. Una búsqueda que se puede combinar · 30 minutos

1. Amplía el repositorio con JpaSpecificationExecutor. Implementa un único buscador con PredicateSpecification<Juego>. Combina los géneros por pertenencia y el precio mínimo inclusivo con AND. Un filtro ausente se omite. Ejecuta findBy(predicado, q -> q.page(pageable)) y reutiliza la paginación de la sesión 6.
2. Completa las firmas HTTP de los tres primeros métodos. Recibe los filtros rápidos con @RequestParam individuales, los del POST con @RequestBody Tipos.Filtro y los flexibles con @RequestParam MultiValueMap<String,String>. En las tres rutas acepta page y size en la URL. Las dos rutas restantes son accesos directos al mismo buscador (PUZLE y AVENTURA con mínimo 10, respectivamente).
3. Admite PUZLE, CARRERAS y AVENTURA. Devuelve 400 para un género desconocido, precio negativo o no entero. En flexible acepta genero repetido como alternativas y rechaza escalares repetidos o claves desconocidas. En rápida los parámetros se usan una sola vez. Una lista vacía en el DTO equivale a filtro ausente.

| Operación y entrada de prueba | Estado | Resultado esperado |
|---|---:|---|
| `GET /buscar/rapida?genero=CARRERAS&minimo=15` | 200 | `{"ids":["J2"],"total":1,"paginas":1}` |
| `POST /buscar/avanzada`<br>Cuerpo `{"generos":["PUZLE","CARRERAS"],"minimo":15}` | 200 | `{"ids":["J2","J4"],"total":2,"paginas":1}` |
| `GET /buscar/flexible?genero=PUZLE&genero=CARRERAS&minimo=15&size=1&page=1` | 200 | `{"ids":["J4"],"total":2,"paginas":2}` |
| `GET /buscar/puzles` | 200 | `{"ids":["J1","J4"],"total":2,"paginas":1}` |
| `GET /buscar/aventuras-premium` | 200 | `{"ids":["J3"],"total":1,"paginas":1}` |

**Comprobación y entrega.** Prueba la búsqueda flexible sin filtros (total 6), minimo=-1 (400), genero=OTRO (400) y minimo=5&minimo=10 (400). 
