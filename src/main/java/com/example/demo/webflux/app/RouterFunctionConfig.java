package com.example.demo.webflux.app;

import com.example.demo.webflux.app.handler.ProductHandler;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
@Data
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/v2/products")
                .or(RequestPredicates.GET("/api/v3/products")), productHandler::list)
                .andRoute(RequestPredicates.GET("/api/v2/products/{id}")
                        .and(contentType(MediaType.APPLICATION_JSON)), productHandler::see)
                .andRoute(RequestPredicates.POST("/api/v2/products"),productHandler::create)
                .andRoute(RequestPredicates.PUT("/api/v2/products/{id}"), productHandler::edit)
                .andRoute(RequestPredicates.DELETE("/api/v2/products/{id}"), productHandler::delete)
                .andRoute(RequestPredicates.POST("/api/v2/products/upload/{id}"),productHandler::upload)
                .andRoute(RequestPredicates.POST("/api/v2/products/create"),productHandler::createwithpictureupload);
    }

}
