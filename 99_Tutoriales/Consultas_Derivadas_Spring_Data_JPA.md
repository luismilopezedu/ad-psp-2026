# Consultas derivadas a partir del nombre del método en Spring Data JPA

## ¿Qué vas a aprender?

Al terminar esta sesión serás capaz de:

1. Explicar qué es una consulta derivada y cómo Spring Data JPA interpreta el nombre de un método.
2. Declarar métodos de búsqueda por igualdad, comparación, texto y varias condiciones.
3. Ordenar resultados y elegir un tipo de retorno adecuado.
4. Ejecutar y verificar consultas sin escribir SQL ni JPQL.
5. Reconocer errores frecuentes en los nombres de los métodos.

## Antes de empezar: nuestro proyecto de cartelera

Trabajaremos con una aplicación Spring Boot que gestiona películas. El proyecto de partida —que facilitará el docente— ya tendrá configurados Spring Data JPA y una base de datos H2, así como una entidad `Pelicula`, un repositorio y datos de ejemplo. **En esta sesión no es necesario crear el proyecto ni desarrollar servicios o controladores REST.**

La entidad `Pelicula` tendrá estos atributos Java:

| Atributo | Tipo Java | Significado |
| --- | --- | --- |
| `id` | `Long` | Identificador de la película |
| `titulo` | `String` | Título |
| `genero` | `String` | Género cinematográfico |
| `duracionMinutos` | `int` | Duración total en minutos |
| `disponible` | `boolean` | Indica si está disponible en cartelera |
| `fechaEstreno` | `LocalDate` | Fecha de estreno |

### Datos de ejemplo

Estos son los registros previstos para el proyecto de partida. Nos permitirán anticipar y comprobar los resultados de algunas consultas.

| Título | Género | Duración | Disponible | Estreno |
| --- | --- | ---: | :---: | --- |
| La órbita perdida | Ciencia ficción | 125 | Sí | 2023-04-21 |
| Rumbo al norte | Aventuras | 95 | Sí | 2020-08-14 |
| Sombras de invierno | Suspense | 110 | No | 2022-11-03 |
| Código rojo | Acción | 105 | Sí | 2024-05-10 |
| Última frontera | Acción | 130 | No | 2021-09-17 |
| El gran viaje | Aventuras | 140 | Sí | 2025-02-07 |
| Misión Delta | Acción | 90 | Sí | 2026-01-15 |
| A ritmo de jazz | Musical | 102 | Sí | 2019-06-28 |
| Crónicas del mar | Drama | 115 | No | 2023-12-01 |
| La última escena | Drama | 85 | Sí | 2020-01-10 |
| Acción en la sombra | Acción | 145 | Sí | 2025-07-18 |
| Recuerdos de abril | Drama | 100 | Sí | 2024-04-12 |

---

## 1. Reto inicial: ¿buscar sin escribir SQL? (5 minutos)

Imagina que necesitas mostrar todas las películas de **Acción**. Con SQL podrías pensar en una consulta como esta:

```sql
SELECT * FROM peliculas WHERE genero = 'Acción';
```

Pero en Spring Data JPA puedes declarar el siguiente método en el repositorio:

```java
List<Pelicula> findByGenero(String genero);
```

Después, desde una clase que utilice el repositorio, puedes invocarlo así:

```java
List<Pelicula> peliculas = peliculaRepository.findByGenero("Acción");
```

Spring Data JPA interpreta el nombre, prepara la consulta y recupera los resultados. **No tienes que implementar el método ni escribir `@Query`.**

**Pregunta para pensar:** ¿qué parte del nombre indica el atributo que quieres consultar?

## 2. Aprende el lenguaje de las consultas derivadas 

### 2.1. La estructura de un método

Una forma habitual de escribir estas consultas es:

```text
findBy + Propiedad + Operador + [And/Or + otra condición] + [OrderBy + Propiedad + Asc/Desc]
```

En el ejemplo `findByDuracionMinutosGreaterThan(120)`:

- `find` indica que queremos recuperar resultados.
- `By` introduce las condiciones de búsqueda.
- `DuracionMinutos` identifica la propiedad Java `duracionMinutos`.
- `GreaterThan` significa **mayor que**.
- El argumento `120` es el valor con el que se realiza la comparación.

**Importante:** se utilizan los **nombres de los atributos de la entidad Java**, no necesariamente los nombres de las columnas SQL. Si la entidad tuviera una propiedad `duracionMinutos` mapeada a la columna `duracion_minutos`, el nombre derivado seguiría usando `DuracionMinutos`.

### 2.2. Operadores que utilizaremos

| Necesidad | Método de ejemplo | Efecto |
| --- | --- | --- |
| Igualdad | `findByGenero(String genero)` | Género igual al argumento |
| Mayor que | `findByDuracionMinutosGreaterThan(int minutos)` | Duración `> minutos` |
| Mayor o igual | `findByDuracionMinutosGreaterThanEqual(int minutos)` | Duración `>= minutos` |
| Menor o igual | `findByDuracionMinutosLessThanEqual(int minutos)` | Duración `<= minutos` |
| Texto contenido | `findByTituloContaining(String texto)` | Título que contiene el texto |
| Ignorar mayúsculas | `findByTituloContainingIgnoreCase(String texto)` | Búsqueda textual sin distinguir mayúsculas/minúsculas |
| Comienzo de texto | `findByTituloStartingWith(String prefijo)` | Título que empieza por un prefijo |
| Dos condiciones | `findByGeneroAndDisponibleTrue(String genero)` | Coinciden género y disponibilidad |
| Alternativa | `findByGeneroOrDuracionMinutosLessThan(String genero, int minutos)` | Se cumple al menos una condición |
| Ordenación | `findByDisponibleTrueOrderByTituloAsc()` | Disponibles, por título ascendente |
| Conteo | `countByGenero(String genero)` | Número de películas de un género |
| Existencia | `existsByTitulo(String titulo)` | `true` si existe alguna coincidencia |
| Fechas | `findByFechaEstrenoAfter(LocalDate fecha)` | Estrenadas después de la fecha |

No necesitas memorizar toda la tabla: aprende la estructura, identifica los operadores y consulta la referencia cuando lo necesites.

### 2.3. ¿Qué tipo de resultado debe devolver el método?

```java
List<Pelicula> findByGenero(String genero);
long countByGenero(String genero);
boolean existsByTitulo(String titulo);
```

- `List<Pelicula>`: devuelve cero, una o varias películas; si no hay coincidencias, la lista está vacía.
- `long`: devuelve una cantidad.
- `boolean`: responde si existe al menos una coincidencia.
- `Optional<Pelicula>`: es apropiado si la búsqueda debe producir **como máximo una** película; si encuentra varias, no las convierte en una lista, sino que produce un error por multiplicidad.

**Atención:** una búsqueda por `genero` puede devolver muchas películas. Por eso no conviene declararla como `Optional<Pelicula>`.

### 2.4. ¿Cuándo se genera la consulta?

El repositorio se define como una **interfaz**. Spring Data JPA crea la implementación y analiza los métodos declarados. Cuando se invoca un método de búsqueda, ejecuta la consulta correspondiente a través de JPA/Hibernate. Los nombres mal formados o las propiedades inexistentes suelen provocar errores durante el arranque del contexto de Spring.

## 3. Programación guiada con el docente 

Abre el proyecto inicial y localiza `PeliculaRepository`. Partiremos de esta estructura:

```java
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {
    // Aquí añadiremos nuestras consultas derivadas.
}
```

### Paso 1. Buscar películas por género

Añade al repositorio:

```java
List<Pelicula> findByGenero(String genero);
```

Ejecuta la llamada desde el comprobador incluido en el proyecto:

```java
peliculaRepository.findByGenero("Acción");
```

**Comprobación:** con nuestros datos deben encontrarse **4 películas**. No se garantiza el orden de aparición si no se especifica una ordenación.

### Paso 2. Buscar por duración

Añade:

```java
List<Pelicula> findByDuracionMinutosGreaterThan(int minutos);
```

Invoca:

```java
peliculaRepository.findByDuracionMinutosGreaterThan(120);
```

**Comprobación:** deben aparecer **4 películas** de más de 120 minutos. ¿Entraría una película que durase exactamente 120 minutos? Explica por qué.

### Paso 3. Buscar parte de un título

Añade:

```java
List<Pelicula> findByTituloContainingIgnoreCase(String texto);
```

Invoca:

```java
peliculaRepository.findByTituloContainingIgnoreCase("CÓDIGO");
```

**Comprobación:** debe encontrarse **Código rojo** aunque el argumento esté escrito con mayúsculas.

### Paso 4. Examina el SQL generado

Observa la consola de la aplicación. Con el registro SQL habilitado en el proyecto, Hibernate mostrará las sentencias que utiliza. El aspecto concreto depende de la versión y del dialecto de base de datos, pero la idea será parecida a esta:

```sql
select p.* from peliculas p where p.genero = ?;
```

El símbolo `?` representa un parámetro. **Declarar** un método no ejecuta por sí solo una consulta: es su **invocación** la que provoca la recuperación de datos.

**Pregunta de comprobación:** ¿qué ventaja tiene utilizar argumentos en los métodos en lugar de escribir el valor de búsqueda dentro de una consulta SQL distinta para cada caso?

## 4. Reto en parejas: construye el buscador de películas 

Vuestra tarea consiste en ampliar `PeliculaRepository` con **cuatro consultas** y comprobarlas mediante la clase de ejecución que proporciona el docente. No hace falta modificar entidades, crear servicios ni añadir endpoints REST.

### Reparto del trabajo

- **Minutos 0–10:** una persona escribe el código; la otra interpreta cada requisito, anticipa el resultado y revisa los nombres de los métodos.
- **Minutos 10–20:** intercambiad los papeles. Ambos debéis saber justificar todas las consultas.

### Reto 1. Género y duración máxima

Recuperad las películas de un género determinado cuya duración sea **menor o igual** que un máximo indicado.

**Comprobación sugerida:** género `Acción` y duración máxima `105`. Con los datos de ejemplo deben aparecer **2 películas**.

### Reto 2. Búsqueda flexible de títulos

Buscad las películas cuyo título **contenga** un texto introducido por el usuario, sin distinguir mayúsculas de minúsculas.

**Comprobación sugerida:** buscad `CÓDIGO` y `código`. Ambas búsquedas deben localizar la misma película.

### Reto 3. Películas disponibles en orden alfabético

Recuperad solamente las películas disponibles y mostradlas ordenadas por título de forma ascendente.

**Comprobación sugerida:** deben aparecer **9 películas**, ordenadas. El orden exacto de caracteres acentuados puede depender de la configuración de la base de datos.

### Reto 4. Conteo por género

Implementad un método que devuelva **cuántas** películas pertenecen a un género, sin necesitar recuperar toda la lista.

**Comprobación sugerida:** para `Acción` el resultado debe ser **4**.

### Ampliación (solo si termináis antes)

Recuperad las **tres películas disponibles de mayor duración**. Investigad las palabras `Top3` y `OrderBy…Desc`. Comprobad que el límite se aplica a los resultados de la consulta y no a una lista que hayáis recortado posteriormente en Java.

### Entrega de la actividad

Debéis tener los cuatro métodos declarados, las llamadas ejecutadas y los resultados comprobados. Cada integrante de la pareja debe poder explicar qué significa uno de los nombres de método que habéis escrito.

## 5. Encuentra el error (8 minutos)

En parejas, leed estos tres casos. Para cada uno, **identificad el problema, proponed una corrección y explicad por qué**.

**Caso A. Propiedad inexistente**

```java
List<Pelicula> findByDuracionMinutoLessThan(int minutos);
```

En la entidad el atributo se llama `duracionMinutos`.

**Caso B. Resultado con varias coincidencias**

```java
Optional<Pelicula> findByGenero(String genero);
```

En la base de datos hay varias películas del género `Acción`.

**Caso C. Condición incorrecta**

```java
List<Pelicula> findByDuracionMinutosGreaterThan(int minutos);
```

El requisito de negocio dice: «Recuperar películas cuya duración sea **mayor o igual** al valor indicado».

Compartiremos las propuestas y discutiremos cómo distinguir un error de escritura de un error en la lógica de búsqueda.

## 6. Ticket de salida: comprueba lo aprendido 

Responde **individualmente y sin ayuda del compañero**:

1. ¿Qué consulta expresa `findByGeneroAndDisponibleTrue(String genero)`?
2. ¿Cuál es la diferencia entre `Containing` y `StartingWith`?
3. Escribe la firma de un método de repositorio que recupere películas estrenadas después de una fecha recibida como parámetro.

## Recuerda estas cinco ideas

1. Las consultas derivadas se **declaran en una interfaz de repositorio**: no escribes su implementación.
2. Spring Data JPA interpreta **propiedades Java** y palabras clave reconocidas; no debes inventar operadores.
3. `GreaterThan` y `GreaterThanEqual` **no significan lo mismo**.
4. Elige el retorno según lo que buscas: colección, resultado único opcional, cantidad o existencia.
5. **Comprueba los resultados con datos conocidos** y, cuando sea útil, observa el SQL que ejecuta Hibernate.

## Documentación para ampliar

- [Spring Data JPA — JPA Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)
- [Spring Data — Defining Query Methods](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html)
- [Spring Data — Repository Query Keywords](https://docs.spring.io/spring-data/jpa/reference/repositories/query-keywords-reference.html)
- [Spring Data — Repository Query Return Types](https://docs.spring.io/spring-data/jpa/reference/repositories/query-return-types-reference.html)

