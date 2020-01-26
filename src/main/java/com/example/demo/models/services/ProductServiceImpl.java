package com.example.demo.models.services;

import com.example.demo.models.dao.CategoriesDao;
import com.example.demo.models.dao.ProductDao;
import com.example.demo.models.documents.Category;
import com.example.demo.models.documents.Product;
import lombok.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Data
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final CategoriesDao categoriesDao;

    @Override
    public Flux<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public Flux<Product> findAllWithNameUpperCase() {
        return productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        });
    }

    @Override
    public Flux<Product> findAllWithNameUpperCaseRepeat() {
        return productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        }).repeat(5000);
    }

    @Override
    public Mono<Product> findById(String id) {
        return productDao.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        return productDao.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return productDao.delete(product);
    }

    @Override
    public Flux<Category> findAllCategories() {
        return categoriesDao.findAll();
    }

    @Override
    public Mono<Category> findByIdCategories(String id) {
        return categoriesDao.findById(id);
    }

    @Override
    public Mono<Category> saveCategories(Category category) {
        return categoriesDao.save(category);
    }
}
