package com.salesianostriana.dam.spring.data.jpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Bean
	CommandLineRunner init(CategoriaRepository categoriaRepository,
						   ProductoRepository productoRepository) {
		return args -> {

			Categoria c = Categoria.builder()
					.nombre("Electrónica")
					.build();

			categoriaRepository.save(c);

			Producto p1 = Producto.builder()
					.nombre("Macbook Pro con pantalla táctil")
					//.categoria(c)
					.precio(3400.0)
					.build();


			c.addProducto(p1);

			System.out.println("Productos en categoria: " + c.getProductos().size());

			productoRepository.save(p1);


			Categoria deBaseDeDatos = categoriaRepository.findById(1L)
					.orElse(null);

			//System.out.println(deBaseDeDatos);
			//deBaseDeDatos.getProductos().forEach(System.out::println);

			Producto productoDeBD = productoRepository.findById(1L)
					.orElse(null);

			System.out.println(productoDeBD.getCategoria().getNombre());

		};
	}



}
