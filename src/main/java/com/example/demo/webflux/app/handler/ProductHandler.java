package com.example.demo.webflux.app.handler;

import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@Component
@Data
public class ProductHandler {

    private final ProductService services;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(services.findAll(), Product.class);
    }

    public Mono<ServerResponse> see(ServerRequest request) {

        String id = request.pathVariable("id");
        return services.findById(id).flatMap(product -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(product))
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> create(ServerRequest request) {

        Mono<Product> product = request.bodyToMono(Product.class);
        return product.flatMap(p -> {

            if(p.getCreateAt() == null){
                p.setCreateAt(new Date());
            }
            return services.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
        .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(p)));
    }

    public Mono<ServerResponse> edit(ServerRequest request) {

        Mono<Product> product = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");

        Mono<Product> productMono = services.findById(id);

        return productMono.zipWith(product, (db,req) -> {
            db.setName(req.getName());
            db.setPrice(req.getPrice());
            db.setCategory(req.getCategory());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(services.save(p), Product.class)
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productMono = services.findById(id);

        return productMono.flatMap(p -> services.delete(p)
                .then(ServerResponse.noContent().build())
        )       .switchIfEmpty(ServerResponse.notFound().build());
    }
}
