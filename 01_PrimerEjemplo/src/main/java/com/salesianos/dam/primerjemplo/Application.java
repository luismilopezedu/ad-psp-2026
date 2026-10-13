package com.salesianos.dam.primerjemplo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info =
	@Info(description = "Una API REST sobre productos",
			version = "1.0",
			contact = @Contact(email = "luismi.lopez@triana.salesianos.edu", name = "Luismi"),
			license = @License(name = "CC BY"),
			title = "API sobre productos"
	)
)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
