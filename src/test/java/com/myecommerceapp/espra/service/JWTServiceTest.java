package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class JWTServiceTest {

    @Autowired
    private JWTService service;
    @Autowired
    private LocalUserDAO dao;

    @Test
    public  void testVerificationTokenNotUsableForLogin(){
        LocalUser user = dao.findByUsernameIgnoreCase("UserA").get();
        String token = service.generateVerificationJWT(user);
        Assertions.assertNull(service.getUsername(token), "Verification token should not contain username");
    }

    @Test
    public void testAuthTokenReturnUsername(){
        LocalUser user = dao.findByUsernameIgnoreCase("UserA").get();
        String token = service.generateJWT(user);
        Assertions.assertEquals(user.getUsername(), service.getUsername(token), "Token for auth should contain users username.");
    }
}
