package com.example.demo.models.dao;

import com.example.demo.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriesDao extends ReactiveMongoRepository<Category,String> {
}
