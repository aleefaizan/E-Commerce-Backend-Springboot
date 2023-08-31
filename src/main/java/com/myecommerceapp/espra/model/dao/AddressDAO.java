package com.myecommerceapp.espra.model.dao;

import com.myecommerceapp.espra.model.Address;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AddressDAO extends ListCrudRepository<Address, Long> {

    List<Address> findByUser_Id(Long localUserId);
}
