# Gestión de Excepciones en una API REST con Spring Boot

## 1. Tipos de excepciones en Java
En Java existen dos categorías principales:

### Excepciones chequeadas (checked)
- Deben declararse o capturarse.
- Ejemplos: `IOException`, `SQLException`.

### Excepciones no chequeadas (unchecked)
- Extienden de `RuntimeException`.
- No requieren declaración ni captura obligatoria.
- Muy usadas en Spring Boot para reglas de negocio, validaciones, etc.

Ejemplo de excepción personalizada:
```java
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Producto con id %d no encontrado".formatted(id));
    }
}
```

## 2. Gestión de excepciones en la capa de servicios
La capa de servicios es responsable de detectar condiciones de error y lanzar excepciones adecuadas.

```java
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
```

## 3. Controlador unificado de errores con @RestControllerAdvice
Spring Boot permite centralizar el manejo de errores en una única clase.

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

}
```

## 4. Uso de ProblemDetail y RFC 7807
Spring Boot 3 incorpora soporte nativo para `ProblemDetail`, la implementación del estándar **RFC 7807** para respuestas de error HTTP.

Ejemplo de creación:
```java
ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
problem.setTitle("Producto no encontrado");
problem.setDetail(ex.getMessage());
```

Ejemplo JSON esperado:
```json
{
  "type": "https://example.com/errors/product-not-found",
  "title": "Producto no encontrado",
  "status": 404,
  "detail": "Producto con id 45 no encontrado",
  "instance": "/api/products/45"
}
```

## 5. Métodos @ExceptionHandler

### Manejo de recurso no encontrado
```java
@ExceptionHandler(ProductNotFoundException.class)
public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Recurso no encontrado");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create("https://example.com/errors/product-not-found"));

    return problem;
}
```

### Parámetros inválidos
```java
@ExceptionHandler(IllegalArgumentException.class)
public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Parámetros inválidos");
    problem.setDetail(ex.getMessage());

    return problem;
}
```

### Error genérico
```java
@ExceptionHandler(Exception.class)
public ProblemDetail handleGeneralException(Exception ex) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Error interno del servidor");
    problem.setDetail("Se ha producido un error inesperado");

    return problem;
}
```

## 6. Sobrescritura de métodos de ResponseEntityExceptionHandler
Ejemplo de validaciones:
```java
@Override
protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Error de validación");
    problem.setDetail("Uno o más campos no son válidos");

    return ResponseEntity.badRequest().body(problem);
}
```

## 7. Controlador REST de ejemplo
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return service.findById(id);
    }
}
```

## 8. Flujo completo de manejo de errores
1. El cliente solicita `/api/products/99`.
2. El servicio no encuentra el registro y lanza `ProductNotFoundException`.
3. `@RestControllerAdvice` captura la excepción.
4. Se crea un `ProblemDetail`.
5. Se devuelve un error estándar RFC 7807.

## 9. Ejemplo final de respuesta
```json
{
  "type": "https://example.com/errors/product-not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "Producto con id 99 no encontrado",
  "instance": "/api/products/99"
}
```

## Conclusión
Este tutorial muestra cómo implementar un sistema limpio, centralizado y profesional para la gestión de errores en una API REST con Spring Boot utilizando `@RestControllerAdvice`, `@ExceptionHandler` y `ProblemDetail`.
