# 🧩 PATRÓN DTO CON JAVA, SPRING Y LOMBOK  
### Guía completa con ejemplos y dos enfoques de transformación (manual y con MapStruct)

---

## 🧠 1. ¿Qué es un DTO y por qué se usa?

Un **DTO (Data Transfer Object)** es un objeto simple que se utiliza para **transportar datos entre distintas capas de una aplicación** sin exponer la lógica interna ni las entidades del dominio directamente.

En una aplicación típica de **Spring Boot**, tenemos varias capas:

```
Controller  →  Service  →  Repository  →  Database
```

Las **entidades JPA** representan las tablas de la base de datos y suelen incluir detalles técnicos (anotaciones de persistencia, relaciones, etc.) que **no deberían ser expuestos al cliente**.

El patrón DTO permite desacoplar la **representación interna** de los datos (entidad) de la **representación externa** (lo que se envía o recibe por la API).

---

## 🧩 2. Ventajas del uso de DTOs

| Beneficio | Descripción |
|------------|-------------|
| **Encapsulación** | Evita exponer directamente las entidades JPA. |
| **Seguridad** | Permite excluir información sensible (contraseñas, tokens, IDs internos, etc.). |
| **Flexibilidad** | Posibilita estructurar los datos según las necesidades de la vista o el cliente. |
| **Compatibilidad** | Facilita la evolución del modelo sin romper la API pública. |
| **Rendimiento** | Se pueden enviar solo los campos necesarios, reduciendo la carga de red. |

---

## 🧱 3. Ejemplo base: entidad `User`

```java
package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

---

## 🧾 4. Creación del DTO

```java
package com.example.demo.dto;

import java.time.LocalDateTime;

public record UserDTO(Long id, String username, String email, LocalDateTime createdAt) {}
```

✅ **Ventajas de usar `record`:**
- Son **inmutables**, lo que garantiza la integridad de los datos transferidos.  
- Generan automáticamente constructor, `equals()`, `hashCode()` y `toString()`.  
- Reducen el ruido de código comparado con POJOs tradicionales.

---

## 🔁 5. Conversión entre Entidad y DTO

### 🔹 Enfoque 1: Conversión manual con Lombok

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

### 🧪 Uso en el servicio

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserDTO create(UserDTO dto) {
        User user = UserMapper.toEntity(dto);
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }
}
```

✅ **Ventajas**
- Muy controlado y predecible.  
- Sin dependencias adicionales.  
- Ideal para proyectos educativos o pequeños.

⚠️ **Inconvenientes**
- Código repetitivo en aplicaciones grandes.  
- Cada cambio en la entidad implica actualizar manualmente el mapper.

---

## 🚀 6. Enfoque 2: Conversión automática con MapStruct

### 📦 a) Dependencias Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.0</version>
    </dependency>
</dependencies>

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
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### ⚙️ b) Definir el Mapper

```java
package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.dto.UserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO dto);
}
```

### 🔍 c) Personalizar el mapeo

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "username", target = "name")
    @Mapping(target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserDTO toDto(User user);

    @InheritInverseConfiguration
    User toEntity(UserDTO dto);
}
```

### 🧪 d) Uso en el servicio

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserDTO create(UserDTO dto) {
        User user = userMapper.toEntity(dto);
        return userMapper.toDto(userRepository.save(user));
    }

    public List<UserDTO> findAll() {
        return userMapper.toDtoList(userRepository.findAll());
    }
}
```

Y el Mapper se puede ampliar para colecciones:

```java
List<UserDTO> toDtoList(List<User> users);
```

### 🧰 e) MapStruct con subobjetos

```java
public class Address {
    private String street;
    private String city;
}
public record AddressDTO(String street, String city) {}

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "address", target = "address")
    UserDTO toDto(User user);

    User toEntity(UserDTO dto);
}
```

---

## 📚 7. Buenas prácticas con DTOs en Spring

| Recomendación | Descripción |
|----------------|-------------|
| **Separar claramente entidades y DTOs** | Ubica DTOs en un paquete distinto (`com.example.demo.dto`). |
| **Usar records para DTOs** | Inmutabilidad, concisión y legibilidad. |
| **Evitar exponer entidades en controladores** | Siempre convertir antes de responder. |
| **Centralizar la lógica de mapeo** | Usa clases Mapper o MapStruct, nunca mezcles en el servicio. |
| **Versionar DTOs si la API evoluciona** | Ejemplo: `UserV1DTO`, `UserV2DTO`. |
| **Añadir validaciones en DTOs** | Con `@NotBlank`, `@Email`, etc., cuando se usan en peticiones. |

---

## 🧭 8. Comparativa final de enfoques

| Criterio | Manual con Lombok | Con MapStruct |
|-----------|------------------|----------------|
| **Dependencias** | Ninguna adicional | Requiere `mapstruct` y `mapstruct-processor` |
| **Rendimiento** | Excelente | Excelente (no usa reflexión) |
| **Mantenimiento** | Medio-alto | Bajo |
| **Curva de aprendizaje** | Baja | Media |
| **Personalización** | Total (manual) | Muy alta (con anotaciones) |
| **Recomendado para** | Proyectos pequeños o educativos | Proyectos medianos/grandes |

---

## 🔚 9. Conclusión

El patrón DTO es **una práctica esencial** para mantener una arquitectura limpia y desacoplada en aplicaciones Spring Boot.

- Si estás desarrollando un proyecto de aprendizaje o pequeño: usa **mapeo manual** con Lombok y records.  
- Si tu aplicación crece, adopta **MapStruct** para automatizar y mantener el código limpio y eficiente.

En ambos casos, los DTOs son una capa de defensa y claridad que **protege tu modelo de dominio** y facilita la evolución de tu API REST.
