package com.example.demo.models.dao;

import com.example.demo.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriesDao extends ReactiveMongoRepository<Category,String> {
    public Mono<Category> findByName(String name);
}
