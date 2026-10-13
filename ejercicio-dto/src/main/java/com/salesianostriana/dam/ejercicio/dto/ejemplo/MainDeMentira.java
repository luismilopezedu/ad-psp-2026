package com.salesianostriana.dam.ejercicio.dto.ejemplo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MainDeMentira {

    //@PostConstruct
    public void main() {

       Miarma miarma = Miarma.builder()
               .id(1L)
               .nombre("Pepe")
               .edad(20)
               .build();


        System.out.println(MiarmaDTO.of(miarma));


    }

}
