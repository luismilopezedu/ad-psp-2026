package com.ejemplo.excepcion;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

 @ExceptionHandler(IllegalArgumentException.class)
 public ProblemDetail filtroIncorrecto(IllegalArgumentException ex) {
  return problema(ex.getMessage());
 }

 @ExceptionHandler(PropertyReferenceException.class)
 public ProblemDetail ordenIncorrecto(PropertyReferenceException ex) {
  return problema("Propiedad de ordenación desconocida: " + ex.getPropertyName());
 }
 private ProblemDetail problema(String detalle) {
  ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
  problema.setTitle("Petición incorrecta");
  return problema;
 }

 @ExceptionHandler(ConstraintViolationException.class)
 public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
  ProblemDetail result = ProblemDetail
          .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

  List<ApiValidationSubError> subErrors =
          ex.getConstraintViolations().stream()
                  .map(ApiValidationSubError::from)
                  .toList();

  result.setProperty("invalid-params", subErrors);

  return result;

 }

 @Override
 protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
  ProblemDetail result = ProblemDetail
          .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Error de validación");

  List<ApiValidationSubError> subErrors =
          ex.getAllErrors().stream()
                  .map(ApiValidationSubError::from)
                  .toList();

  result.setProperty("invalid-params", subErrors);


  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(result);
 }


 @Builder
 record ApiValidationSubError(
         String object,
         String message,
         @JsonInclude(JsonInclude.Include.NON_NULL)
         String field,
         @JsonInclude(JsonInclude.Include.NON_NULL)
         Object rejectedValue
 ) {

  public ApiValidationSubError(String object, String message) {
   this(object, message, null, null);
  }

  public static ApiValidationSubError from(ObjectError error) {

   ApiValidationSubError result = null;

   if (error instanceof FieldError fieldError) {
    result = ApiValidationSubError.builder()
            .object(fieldError.getObjectName())
            .message(fieldError.getDefaultMessage())
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .build();
   } else {
    result = ApiValidationSubError.builder()
            .object(error.getObjectName())
            .message(error.getDefaultMessage())
            .build();
   }

   return result;


  }

  public static ApiValidationSubError from(ConstraintViolation v) {
   return ApiValidationSubError.builder()
           .message(v.getMessage())
           .rejectedValue(v.getInvalidValue())
           .object(v.getRootBeanClass().getSimpleName())
           .field(getFieldName(v))
           .build();
  }

  /*
      Método que permite obtener el nombre del campo sobre
      el que se ha producido la violación
   */
  private static String getFieldName(ConstraintViolation<?> v) {

   String field = null;

   for (Path.Node node : v.getPropertyPath()) {
    if (node.getKind() == ElementKind.PROPERTY || node.getKind() == ElementKind.PARAMETER) {
     field = node.getName() != null ? node.getName() : node.toString();
    }
   }
   return field;
  }
 }

}
