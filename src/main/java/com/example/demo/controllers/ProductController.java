package com.example.demo.controllers;

import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Data
public class ProductController {

    private final ProductService service;

    @Value("${config.upload.path}")
    private String path;

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Product>> upload(@PathVariable String id, @RequestPart FilePart file){
        return service.findById(id).flatMap(p -> {
           p.setPicture(UUID.randomUUID().toString() + "-" + file.filename()
           .replace(" ","")
           .replace(":","")
           .replace("\\",""));

         return file.transferTo(new File(path + p.getPicture())).then(service.save(p));
        }).map(product -> ResponseEntity.ok(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload/v2/")
    public Mono<ResponseEntity<Product>> upload2(Product product, @RequestPart FilePart file) {

        if(product.getCreateAt()==null) {
            product.setCreateAt(new Date());
        }

        product.setPicture(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ","")
                .replace(":","")
                .replace("\\",""));

        return file.transferTo(new File(path + product.getPicture())).then(service.save(product))
                .map(p -> ResponseEntity.created(
                URI.create("/api/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p));

    }

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
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid  Mono<Product> product) {

        Map<String, Object> response = new HashMap<String, Object>();

        return product.flatMap(product1 -> {
            if(product1.getCreateAt()==null) {
                product1.setCreateAt(new Date());
            }
            return service.save(product1).map(p -> {
                response.put("product", p);
                return ResponseEntity.created(
                        URI.create("/api/products/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            });

        }).onErrorResume(t -> {
          return Mono.just(t).cast(WebExchangeBindException.class)
                  .flatMap(e -> Mono.just(e.getFieldErrors()))
                  .flatMapMany(Flux::fromIterable)
                  .map(fieldError -> "El campo " + fieldError.getField() +  " " + fieldError.getDefaultMessage())
                  .collectList()
                  .flatMap(list -> {
                      response.put("errors", list);
                      return Mono.just(ResponseEntity.badRequest().body(response));
                  });
        });



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
