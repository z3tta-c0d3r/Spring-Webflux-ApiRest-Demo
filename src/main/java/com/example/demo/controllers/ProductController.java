package com.example.demo.controllers;

import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@RestController
@RequestMapping("/api/products")
@Data
public class ProductController {

    private final ProductService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> list() {
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> see(@PathVariable String id) {
        return service.findById(id).map(p -> ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build()
        );
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> create(@RequestBody Product product) {

        if(product.getCreateAt()==null) {
            product.setCreateAt(new Date());
        }

        return service.save(product).map(p -> ResponseEntity.created(
                URI.create("/api/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p));

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> update(@RequestBody Product product, @PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            p.setName(product.getName());
            p.setPrice(product.getPrice());
            p.setCategory(product.getCategory());
            return service.save(p);
        }).map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            return service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}
