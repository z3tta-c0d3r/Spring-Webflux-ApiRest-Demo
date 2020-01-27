package com.example.demo.webflux.app.handler;

import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Data
public class ProductHandler {

    private final ProductService services;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(services.findAll(), Product.class);
    }
}
