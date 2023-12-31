package com.myecommerceapp.espra.model.dao;

import com.myecommerceapp.espra.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDAO extends ListCrudRepository<Product, Long> {

    Page<Product> findAll(Pageable pageable);
}
