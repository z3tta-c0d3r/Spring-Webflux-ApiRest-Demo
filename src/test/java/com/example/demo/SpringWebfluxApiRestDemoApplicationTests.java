package com.example.demo;

import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@Slf4j
class SpringWebfluxApiRestDemoApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService productService;

	@Test
	void contextLoads() {
	}

	@Test
	public void listTest() {
		client.get()
				.uri("/api/v2/products")
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
				.uri("/api/v2/products/{id}", Collections.singletonMap("id",productMono.block().getId()))
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

}
