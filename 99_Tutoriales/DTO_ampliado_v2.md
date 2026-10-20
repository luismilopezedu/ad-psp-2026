# 🧩 Patrón DTO con Java, Spring Boot y Lombok

**Tres formas de transformación:** método estático en el `record`, mapper manual y MapStruct.

## 1. ¿Qué es un DTO?

Un **DTO (Data Transfer Object)** transporta datos entre la API y sus clientes sin exponer directamente las entidades JPA. Permite seleccionar campos, ocultar información sensible y adaptar las respuestas sin modificar el modelo.

En Spring Boot, una estructura habitual es:

```text
Controller → Service → Repository → Base de datos
```

| Ventaja | Ejemplo |
|---|---|
| Seguridad | No devolver contraseñas. |
| Flexibilidad | Mostrar solo los campos necesarios. |
| Desacoplamiento | Cambiar una entidad sin alterar el contrato de la API. |

## 2. Nuestro modelo: `User`

Partimos de una entidad gestionada por JPA y Lombok:

```java
package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
}
```

La API no debería devolver `password`. Crearemos un DTO de salida con los cuatro campos públicos.

## 3. Tres formas de transformar una entidad a DTO

### 3.1. Primera forma: método estático `of()` dentro del `record`

Esta es la opción más directa para comenzar. **El propio DTO sabe construirse a partir de una entidad**:

```java
package com.example.demo.dto;

import com.example.demo.domain.User;
import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String username,
        String email,
        LocalDateTime createdAt
) {
    public static UserDTO of(User user) {
        if (user == null) return null;

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
```

Un `record` genera constructor, accesores como `username()`, `equals()`, `hashCode()` y `toString()`. También admite métodos estáticos, como `of()`.

Uso:

```java
UserDTO dto = UserDTO.of(user);

// En un servicio:
return userRepository.findById(id)
        .map(UserDTO::of)
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
```

El control inicial de `null` evita un `NullPointerException` si no recibimos ninguna entidad. No hace falta una clase mapper adicional.

### 3.2. Segunda forma: mapper manual independiente

Si preferimos separar la transformación del DTO, podemos crear `UserMapper` y aprovechar `@UtilityClass` de Lombok para disponer de métodos estáticos:

```java
package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.dto.UserDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    public UserDTO toDto(User user) {
        if (user == null) return null;

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.id())
                .username(dto.username())
                .email(dto.email())
                .createdAt(dto.createdAt())
                .build();
    }
}
```

```java
UserDTO dto = UserMapper.toDto(user);
User user = UserMapper.toEntity(dto);
```

**Atención:** `toEntity()` no recupera la contraseña porque `UserDTO` no la contiene. En un alta real conviene usar un DTO de entrada específico con los datos requeridos.

### 3.3. Tercera forma: transformación automática con MapStruct

MapStruct genera el código de conversión durante la compilación. En Maven añadimos su dependencia:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.0</version>
</dependency>
```

También configuramos el procesador de anotaciones. Como usamos Lombok, incluimos su integración con MapStruct (el ejemplo presupone el `lombok.version` proporcionado por Spring Boot):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.6.0</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Creamos una **interfaz**, no una clase con la conversión escrita a mano:

```java
package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.dto.UserDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapStructMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO dto);
    List<UserDTO> toDtoList(List<User> users);
}
```

Spring puede inyectar el mapper generado en un servicio:

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapStructMapper userMapper;

    public List<UserDTO> findAll() {
        return userMapper.toDtoList(userRepository.findAll());
    }
}
```

También permite configurar correspondencias entre campos con nombres distintos mediante `@Mapping`.

## 4. DTO compuesto: un DTO dentro de otro

Queremos devolver un usuario con su dirección, pero **sin insertar la entidad `Address` directamente en el DTO**.

### 4.1. Modelo

```java
package com.example.demo.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Address {
    private String street;
    private String city;
}
```

Añadimos a `User` este atributo (con su getter generado por Lombok):

```java
@Embedded
private Address address;
```

`@Embeddable` y `@Embedded` permiten almacenar la dirección como parte del usuario; para el DTO nos interesa que `getAddress()` devuelve un objeto o `null`.

### 4.2. Primera forma: métodos `of()` dentro de los DTO

Empezamos por el DTO más pequeño:

```java
package com.example.demo.dto;

import com.example.demo.domain.Address;

public record AddressDTO(String street, String city) {
    public static AddressDTO of(Address address) {
        if (address == null) return null;
        return new AddressDTO(address.getStreet(), address.getCity());
    }
}
```

Ahora creamos un DTO que lo incorpora como campo:

```java
package com.example.demo.dto;

import com.example.demo.domain.User;
import java.time.LocalDateTime;

public record UserWithAddressDTO(
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        AddressDTO address
) {
    public static UserWithAddressDTO of(User user) {
        if (user == null) return null;

        return new UserWithAddressDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                AddressDTO.of(user.getAddress())
        );
    }
}
```

**Fíjate en `AddressDTO.of(user.getAddress())`:** transforma primero la dirección y después la incorpora al DTO principal. Si no existe, el campo vale `null`.

### 4.3. Segunda forma: mapper manual

También podemos usar dos mappers. Cada clase va en su propio archivo (se omiten `package` e `import`):

```java
@UtilityClass
public class AddressMapper {
    public AddressDTO toDto(Address address) {
        if (address == null) return null;
        return new AddressDTO(address.getStreet(), address.getCity());
    }
}

@UtilityClass
public class UserWithAddressMapper {
    public UserWithAddressDTO toDto(User user) {
        if (user == null) return null;
        return new UserWithAddressDTO(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getCreatedAt(), AddressMapper.toDto(user.getAddress())
        );
    }
}
```

MapStruct también puede generar este mapeo si declaramos los dos métodos `toDto(User)` y `toDto(Address)` en una interfaz `@Mapper`.

### 4.4. Resultado JSON

```json
{
  "id": 7,
  "username": "laura",
  "email": "laura@ejemplo.com",
  "createdAt": "2026-10-20T10:30:00",
  "address": {
    "street": "Calle Mayor, 12",
    "city": "Sevilla"
  }
}
```

En JSON, `address` es un objeto dentro de otro. Si la dirección falta, normalmente aparecerá como `null`, salvo que Jackson esté configurado para omitirlo.

## 5. DTO compuesto: una colección de otros DTO

Imagina que `GET /users` debe devolver los usuarios junto con el total. Usaremos `UsersDTO`, que contiene una **`List<UserDTO>`**, no una `List<User>`.

### 5.1. Primera forma: método `of()` en el DTO de la colección

```java
package com.example.demo.dto;

import com.example.demo.domain.User;
import java.util.List;
import java.util.Objects;

public record UsersDTO(List<UserDTO> users, int total) {
    public static UsersDTO of(List<User> users) {
        if (users == null) return new UsersDTO(List.of(), 0);

        List<UserDTO> dtos = users.stream()
                .filter(Objects::nonNull)
                .map(UserDTO::of)
                .toList();

        return new UsersDTO(dtos, dtos.size());
    }
}
```

La secuencia realiza tres operaciones: descarta elementos `null`, transforma cada `User` mediante `UserDTO.of()` y obtiene una lista de DTO. `toList()` requiere Java 16 o superior.

Así, una lista nula o vacía devuelve `users: []` y `total: 0`; los elementos nulos tampoco provocan errores. El total cuenta los elementos convertidos.

```java
// Dentro del servicio:
public UsersDTO findAllWithTotal() {
    return UsersDTO.of(userRepository.findAll());
}
```

### 5.2. Segunda forma: mapper manual de colecciones

Podemos conseguir el mismo resultado con una clase externa (se omiten `package` e `import`):

```java
@UtilityClass
public class UsersMapper {
    public UsersDTO toDto(List<User> users) {
        if (users == null) return new UsersDTO(List.of(), 0);

        List<UserDTO> dtos = users.stream()
                .filter(Objects::nonNull)
                .map(UserMapper::toDto)
                .toList();

        return new UsersDTO(dtos, dtos.size());
    }
}
```

MapStruct permite igualmente transformar una lista declarando `List<UserDTO> toDtoList(List<User> users)`; el DTO envoltorio con `total` requiere indicar cómo calcular ese campo.

### 5.3. Resultado JSON

```json
{
  "users": [
    {"id": 1, "username": "ana", "email": "ana@ejemplo.com", "createdAt": "2026-10-18T09:00:00"},
    {"id": 2, "username": "pablo", "email": "pablo@ejemplo.com", "createdAt": "2026-10-19T12:15:00"}
  ],
  "total": 2
}
```

Aquí `users` es un array de objetos JSON. **No basta con pasar la lista de entidades**: hay que convertir sus elementos.

## 6. Buenas prácticas y comparativa

- Usa `record` para DTO de salida y no expongas contraseñas ni entidades completas.
- Controla referencias y colecciones `null` antes de acceder a sus datos.
- Centraliza la transformación: en `of()` o en mappers, evitando duplicarla en los servicios.
- Para peticiones de entrada, define DTO específicos y aplica validación (`@NotBlank`, `@Email`, etc.).

| Enfoque | ¿Dónde está la conversión? | ¿Cuándo usarlo? |
|---|---|---|
| **1. Método `of()`** | Dentro del propio `record` | Ejercicios y conversiones sencillas. |
| **2. Mapper manual** | En una clase independiente | Transformaciones con más lógica o reutilización. |
| **3. MapStruct** | Código generado desde una interfaz | Proyectos con muchos DTO y mapeos. |

**Idea clave:** un DTO puede incluir valores sencillos, otro DTO o una colección de DTO. En los dos últimos casos, transforma también cada objeto interior. Los tres enfoques consiguen el mismo resultado; cambia **dónde escribimos —o generamos— esa transformación**.
