package com.example.demo.webflux.app.handler;

import com.example.demo.models.documents.Category;
import com.example.demo.models.documents.Product;
import com.example.demo.models.services.ProductService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
@Data
public class ProductHandler {

    private final ProductService services;

    @Value("${config.upload.path}")
    private String path;

    private final Validator validator;

    public Mono<ServerResponse> upload(ServerRequest request) {

        String id = request.pathVariable("id");

        return request.multipartData().map(mp -> mp.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> services.findById(id).flatMap(p -> {
                    String sfile = UUID.randomUUID().toString() + "_" + file.filename()
                            .replace(" ","_")
                            .replace(":","")
                            .replace("\\","");

                    p.setPicture(sfile);
                    return file.transferTo(new File(path + p.getPicture())).then(services.save(p));
                })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(services.save(p), Product.class)
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> createwithpictureupload(ServerRequest request) {

        Mono<Product> productMono = request.multipartData().map(mp -> {
            FormFieldPart name = (FormFieldPart) mp.toSingleValueMap().get("name");
            FormFieldPart price = (FormFieldPart) mp.toSingleValueMap().get("price");
            FormFieldPart categoryId = (FormFieldPart) mp.toSingleValueMap().get("categoryid");
            FormFieldPart categoryName = (FormFieldPart) mp.toSingleValueMap().get("categoryname");

            Category category = Category.builder()
                    .name(categoryName.value())
                    .id(categoryId.value()).build();

            return Product.builder()
                    .name(name.value())
                    .price(Double.parseDouble(price.value()))
                    .category(category).build();
        });

        return request.multipartData().map(mp -> mp.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productMono.flatMap(p -> {
                    String sfile = UUID.randomUUID().toString() + "_" + file.filename()
                            .replace(" ","_")
                            .replace(":","")
                            .replace("\\","");

                    p.setPicture(sfile);
                    p.setCreateAt(new Date());
                    return file.transferTo(new File(path + p.getPicture())).then(services.save(p));
                })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(services.save(p), Product.class)
                        );
    }

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

            Errors errors =  new BeanPropertyBindingResult(p,Product.class.getName());
            validator.validate(p,errors);

            if(errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromObject(list)));
            } else {
                if(p.getCreateAt() == null){
                    p.setCreateAt(new Date());
                }
            }
            return services.save(p).flatMap(pdb -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(pdb)));
        });
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
