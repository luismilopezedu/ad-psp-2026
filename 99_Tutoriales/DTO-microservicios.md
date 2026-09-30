# Estrategias de diseño de DTOs en microservicios

## Estrategias para diseñar DTOs en microservicios

### 1) API-first y contratos claros
- **Parte del contrato**: diseña primero el **OpenAPI/JSON Schema** (API-first). El código vendrá después.
- **Boundary por servicio**: cada microservicio define **sus** DTOs. No compartas clases entre servicios; comparte **esquemas** si es necesario, pero evita acoplar código.

### 2) Un DTO por caso de uso (no por entidad)
Piensa en **operaciones**, no en tablas:
- **Request DTOs**: `CreateXRequest`, `UpdateXRequest`, `PatchXRequest`.
- **Response DTOs**: `XSummary`, `XDetail`, `CreatedXResponse`.
- **Queries**: DTOs para filtros (`ListXQuery`).
- **CQRS**: separa modelos de **comando** y **consulta**.

### 3) Granularidad de respuestas
- **Summary vs Detail**: respuestas ligeras para listas y ricas para detalle.
- **Expansiones**: `?include=` o `?fields=` para campos opcionales.

### 4) Paginación, orden y filtros
```java
public record PageRequestDTO(Integer page, Integer size, String sort) {}
public record PageResponseDTO<T>(List<T> content, int page, int size, long totalElements) {}
```

### 5) Modelo de errores uniforme
Seguir **Problem Details** (`application/problem+json`):
```java
public record ProblemDTO(
  String type, String title, int status, String detail, String instance,
  Map<String, Object> errors
) {}
```

### 6) Validación y semántica
- Bean Validation en request DTOs.
- Validaciones de coherencia de negocio personalizadas.

### 7) Versionado y compatibilidad
- Versiona el contrato.
- Mantén compatibilidad backward.
- Paquetes `dto.v1`, `dto.v2`.

### 8) Seguridad y datos sensibles
- No exponer secretos.
- DTOs públicos vs internos.

### 9) Nombres y consistencia
- Convenciones claras y consistentes.
- JSON con `camelCase`.

### 10) Mapeo y desacoplo
- MapStruct o mapping manual.
- DTOs sin anotaciones JPA.

### 11) PATCH realista
- JSON Patch o JSON Merge Patch.

### 12) Hiper-media ligera y metadatos
- Links básicos y trazabilidad (`correlationId`).

---

## ¿Cómo saber si has diseñado suficientes DTOs?

### Señales de que faltan DTOs
- Listados devuelven DTOs de detalle.
- Un único DTO sirve para múltiples operaciones confusas.
- Repetición de subconjuntos de campos.

### Señales de que sobran DTOs
- Variantes casi idénticas sin valor añadido.
- Cambios en cascada constantes.

### Regla útil
> **Un DTO por intención de uso**.

---

## Checklist de calidad de DTOs

### Funcional
- Campos necesarios y suficientes.
- Tipos correctos.
- Validaciones declaradas.
- Errores coherentes.
- Paginación estándar.

### Compatibilidad
- Cambios no rompientes.
- Versionado planificado.

### Consistencia
- Nombres y formatos uniformes.
- Política clara de `null`.

### Seguridad
- Sin datos sensibles.
- Minimización de datos personales.

---

## Verificación del diseño

### 1) Validación del contrato
- OpenAPI + linting.
- Detección de cambios breaking.

### 2) Contract Testing
- Pact / Spring Cloud Contract.

### 3) Tests
- Serialización JSON.
- Tests de mapeo.
- Fuzzing.

### 4) Ejemplos y snapshots
- Ejemplos JSON versionados.
- Tests de snapshot.

### 5) Validación con muestras reales
- Colecciones Postman.
- Validación contra schema.

---

## Ejemplo: Microservicio de Pedidos

```java
public record CreateOrderRequest(UUID customerId, List<OrderLineInput> lines, AddressInput shippingAddress) {}
public record OrderSummary(UUID id, UUID customerId, BigDecimal total, String status, Instant createdAt) {}
public record OrderDetail(UUID id, UUID customerId, List<OrderLine> lines, Address shippingAddress,
                          BigDecimal subtotal, BigDecimal taxes, BigDecimal total, String status, Instant createdAt) {}
public record ProblemDTO(String type, String title, int status, String detail, String instance, Map<String,Object> errors) {}
```

---

## Conclusión

- Diseña DTOs por intención.
- Contratos primero.
- Versiona y valida siempre.
- Usa tests de contrato y ejemplos reales.
