package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.WebOrder;
import com.myecommerceapp.espra.model.dao.WebOrderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebOrderService {

    @Autowired
    private WebOrderDAO dao;

    public List<WebOrder> getOrders(LocalUser user){
        return dao.findByUser(user);
    }
}
