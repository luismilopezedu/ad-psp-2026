package com.ejemplo;

import com.ejemplo.config.DevDataInitializer;
import com.ejemplo.demo.ConsultasDemo;
import com.ejemplo.demo.GrafosEntidadDemo;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

class PerfilesTest {
 @Test void produccionExcluyeInicializadorYDemostraciones() {
  // No requiere base de datos: solo comprueba la selección de estos componentes.
  comprobarExclusion("prod");
  comprobarExclusion("dev", "prod");
 }
 private void comprobarExclusion(String... perfiles) {
  try (var contexto=new AnnotationConfigApplicationContext()) {
   contexto.getEnvironment().setActiveProfiles(perfiles);
   contexto.register(DevDataInitializer.class, GrafosEntidadDemo.class, ConsultasDemo.class);
   contexto.refresh();
   assertThat(contexto.getBeansOfType(DevDataInitializer.class)).isEmpty();
   assertThat(contexto.getBeansOfType(GrafosEntidadDemo.class)).isEmpty();
   assertThat(contexto.getBeansOfType(ConsultasDemo.class)).isEmpty();
  }
 }
}
