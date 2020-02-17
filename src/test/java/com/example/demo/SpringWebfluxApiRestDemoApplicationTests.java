package com.example.demo;

import com.example.demo.models.documents.Category;
import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@AutoConfigureWebTestClient // NO APAARECE EL PUERTO DEL NETTY
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RunWith(SpringRunner.class)
@Slf4j
class SpringWebfluxApiRestDemoApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService productService;

	@Value("${config.base.endpoint}")
	private String url;

	@Test
	void contextLoads() {
	}

	@Test
	public void listTest() {
		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				//.hasSize(9); Fase 1
				.consumeWith(response -> {
					List<Product> productList = response.getResponseBody();
					productList.stream().forEach(p ->{
						log.info(p.getName());
					});
					Assertions.assertThat(productList.size()>0).isTrue();
				});
	}

	@Test
	public void seeTest() {

		Mono<Product> productMono = productService.findByName("iRobot Roomba");

		client.get()
				.uri(url +"/{id}", Collections.singletonMap("id",productMono.block().getId()))
				.header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product = response.getResponseBody();

					Assertions.assertThat(product.getId()).isNotEmpty();
					Assertions.assertThat(product.getId().length()>0).isTrue();
					Assertions.assertThat(product.getName()).isEqualTo("iRobot Roomba");
				});
				/*Forma1
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("iRobot Roomba");
				 */
	}

	@Test
	public void crearTest() {

		Category categoria = productService.findByCategoryName("Generico").block();

		Product product = Product.builder().name("Table").price(100.12).category(categoria).build();
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product),Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Table")
				.jsonPath("$.category.name").isEqualTo("Generico");
	}

	@Test
	public void crearTest2() {

		Category categoria = productService.findByCategoryName("Generico").block();

		Product product = Product.builder().name("Table").price(100.12).category(categoria).build();
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product),Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getName()).isEqualTo("Table");
					Assertions.assertThat(p.getCategory().getName()).isNotEmpty();
				});
	}
/*
	// Si quitamos el v2 del base.config tenemos que cambiar el test
	@Test
	public void crearTestSinV2() {

		Category categoria = productService.findByCategoryName("Generico").block();

		Product product = Product.builder().name("Table").price(100.12).category(categoria).build();
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product),Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.product.id").isNotEmpty()
				.jsonPath("$.product.name").isEqualTo("Table")
				.jsonPath("$.product.category.name").isEqualTo("Generico");
	}

	@Test
	public void crear2TestSinV2() {

		Category categoria = productService.findByCategoryName("Generico").block();

		Product product = Product.builder().name("Table").price(100.12).category(categoria).build();
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product),Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
				.consumeWith(response -> {
					Object o = response.getResponseBody().get("product");
					Product p = new ObjectMapper().convertValue(o,Product.class);
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getName()).isEqualTo("Table");
					Assertions.assertThat(p.getCategory().getName()).isNotEmpty();
				});
	}

	@Test
	public void editTest() {
		Product product = productService.findByName("iPhone XR Negro").block();
		Category categoria = productService.findByCategoryName("Informatica").block();

		Product productEdit = Product.builder().name("iPhone XR Pink").price(1000.12).category(categoria).build();

		client.put().uri(url+"/{id}", Collections.singletonMap("id",product.getId()))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productEdit),Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("iPhone XR Pink");


	}
	*/

	@Test
	public void deleteTest() {
		Product product = productService.findByName("iPad Mini Black").block();

		client.delete().uri(url+"/{id}", Collections.singletonMap("id",product.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();
/*
		client.get().uri("/api/v2/products/{id}", Collections.singletonMap("id",product.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.isEmpty();

 */
	}



}
