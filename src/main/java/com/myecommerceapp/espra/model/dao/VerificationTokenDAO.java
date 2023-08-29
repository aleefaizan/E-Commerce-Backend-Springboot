package com.myecommerceapp.espra.model.dao;

import com.myecommerceapp.espra.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {
}
