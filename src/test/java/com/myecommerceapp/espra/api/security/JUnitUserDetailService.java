package com.myecommerceapp.espra.api.security;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JUnitUserDetailService implements UserDetailsService {

    @Autowired
    private LocalUserDAO dao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<LocalUser> optUser = dao.findByUsernameIgnoreCase(username);
        if (optUser.isPresent()){
            return optUser.get();
        }
        return null;
    }
}
