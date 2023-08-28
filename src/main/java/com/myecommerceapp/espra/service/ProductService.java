package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.model.Product;
import com.myecommerceapp.espra.model.dao.ProductDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDAO dao;

    public List<Product> productList(){
        return dao.findAll();
    }
}
