package com.example.demo.models.dao;

import com.example.demo.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductDao extends ReactiveMongoRepository<Product,String> {
}
