# Verificación del proyecto

- Compilación y empaquetado Maven: BUILD SUCCESS.
- Pruebas de integración sobre H2: **6 ejecutadas, 0 fallos, 0 errores, 0 omitidas**.
- Ejecución del JAR: código de salida 0, todas las secciones de ConsultasDemo y marcador final presentes.
- Entorno: Java 21.0.13, Spring Boot 4.1.2, Hibernate 7.4.13.Final, H2 2.4.240.
- Perfil PostgreSQL incluido, no ejecutado en este entorno.

Las comprobaciones cubren todos los métodos de consulta del tutorial, las dos proyecciones DTO, los parámetros Double de compatibilidad, los rangos inclusivos, los precios nulos, la paginación y el acceso a asociaciones cargadas con FETCH fuera de transacción.

El runtime de validación carece de ct.sym (no es un JDK de desarrollo completo). Para compilar se usaron las opciones Maven `-Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21`, sin cambiar el POM entregado. Con un JDK completo, usa los comandos normales del README. Las pruebas y la ejecución del JAR sí se completaron sobre Java 21.

El ZIP contiene fuentes y configuración, no las dependencias ni el directorio target. Maven los genera localmente.
