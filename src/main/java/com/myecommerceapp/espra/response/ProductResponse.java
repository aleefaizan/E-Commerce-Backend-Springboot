package com.myecommerceapp.espra.response;

import com.myecommerceapp.espra.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductResponse {

    private List<Product> productList;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalElement;
    private Integer totalPage;
    private boolean lastPage;
}
