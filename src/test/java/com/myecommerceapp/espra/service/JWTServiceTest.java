package com.myecommerceapp.espra.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class JWTServiceTest {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;
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

    @Test
    public void testJWTNotGeneratedByUs(){
        String token = JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256("Not the real secret"));
        Assertions.assertThrows(SignatureVerificationException.class, () -> service.getUsername(token));
    }

    @Test
    public void testJWTCorrectlySignedNoIssue(){
        String token = JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                () -> service.getUsername(token));
    }
}
