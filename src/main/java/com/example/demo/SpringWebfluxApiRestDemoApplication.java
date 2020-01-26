package com.example.demo;

import com.example.demo.models.documents.Category;
import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@Slf4j
@SpringBootApplication
public class SpringWebfluxApiRestDemoApplication implements CommandLineRunner {

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	@Autowired
	private ProductService pservice;

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxApiRestDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Eliminamos los registros antes de insertar
		reactiveMongoTemplate.dropCollection("Products").subscribe();
		reactiveMongoTemplate.dropCollection("Categories").subscribe();

		// Categories
		Category electrodomestico = Category.builder().name("Electrodomestico").build();
		Category informatica = Category.builder().name("Informatica").build();
		Category generico = Category.builder().name("Generico").build();

		Flux.just(electrodomestico,informatica,generico)
				.flatMap(pservice::saveCategories).doOnNext(c -> {
			log.info("Category -> " + c.getName() + " id -> " + c.getId());
		}).thenMany(		//Import products to MONGODB
				Flux.just(
						Product.builder().name("iRobot Roomba").price(247.08).category(electrodomestico).build(),
						Product.builder().name("Rowenta Calentador").price(34.98).category(electrodomestico).build(),
						Product.builder().name("iPencil Apple").price(87.98).category(informatica).build(),
						Product.builder().name("IPad 10.2").price(454.28).category(informatica).build(),
						Product.builder().name("iPhone XR Negro").price(704.98).category(informatica).build(),
						Product.builder().name("iPad Mini Black").price(214.08).category(informatica).build(),
						Product.builder().name("Cascos Sennheiser").price(99.00).category(generico).build(),
						Product.builder().name("iPods White").price(100.58).category(informatica).build(),
						Product.builder().name("iWatch 3").price(450.09).category(informatica).build()
				).flatMap(product -> {
					product.setCreateAt(new Date());
					return pservice.save(product);
				})
		).subscribe(product -> log.info("Insert: " + product.getId() + " - " + product.getName()));

	}
}
