package com.myecommerceapp.espra.api.controller.product;

import com.myecommerceapp.espra.response.ProductResponse;
import com.myecommerceapp.espra.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public ProductResponse getAllProducts(@RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
                                          @RequestParam(value = "sortBy", defaultValue = "name", required = false) String sortBy,
                                          @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize){
        return new  ResponseEntity<ProductResponse>(productService.getAllProduct(pageNum, sortBy, pageSize), HttpStatus.OK).getBody();
    }
}
