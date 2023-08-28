package com.myecommerceapp.espra.api.controller.order;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.WebOrder;
import com.myecommerceapp.espra.service.WebOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private WebOrderService orderService;

    @GetMapping
    public List<WebOrder> getOrder(@AuthenticationPrincipal LocalUser user){
        return orderService.getOrders(user);
    }
}
