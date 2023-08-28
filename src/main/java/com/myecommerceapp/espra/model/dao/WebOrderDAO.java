package com.myecommerceapp.espra.model.dao;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebOrderDAO extends ListCrudRepository<WebOrder, Long> {

    List<WebOrder> findByUser(LocalUser user);
}
