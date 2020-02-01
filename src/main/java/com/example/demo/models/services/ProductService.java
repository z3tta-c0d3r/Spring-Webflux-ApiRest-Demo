package com.example.demo.models.services;

import com.example.demo.models.documents.Category;
import com.example.demo.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    public Flux<Product> findAll();
    public Flux<Product> findAllWithNameUpperCase();
    public Flux<Product> findAllWithNameUpperCaseRepeat();
    public Mono<Product> findById(String id);
    public Mono<Product> save(Product product);
    public Mono<Void> delete(Product product);

    public Flux<Category> findAllCategories();
    public Mono<Category> findByIdCategories(String id);
    public Mono<Category> saveCategories(Category category);

    public Mono<Product> findByName(String name);
}
