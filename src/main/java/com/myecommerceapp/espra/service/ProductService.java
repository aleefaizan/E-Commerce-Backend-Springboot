package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.model.Product;
import com.myecommerceapp.espra.model.dao.ProductDAO;
import com.myecommerceapp.espra.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {


    @Autowired
    private ProductDAO dao;

    public ProductResponse getAllProduct(Integer pageNum, String sortBy, Integer pageSize){
        Sort sort = Sort.by(sortBy);
        Pageable p = PageRequest.of(pageNum, pageSize, sort);
        Page<Product> productPage = dao.findAll(p);

        List<Product> products = productPage.getContent();
        ProductResponse response = new ProductResponse();
        response.setProductList(products);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotalElement((int)productPage.getTotalElements());
        response.setTotalPage(productPage.getTotalPages());
        response.setLastPage(productPage.isLast());

        return response;
    }

    public List<Product> productList(){
        return dao.findAll();
    }
}
