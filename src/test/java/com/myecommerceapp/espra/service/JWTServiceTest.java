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
    public void testLoginJWTNotGeneratedByUs(){
        String token = JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256("Not the real secret"));
        Assertions.assertThrows(SignatureVerificationException.class, () -> service.getUsername(token));
    }

    @Test
    public void testLoginJWTCorrectlySignedNoIssue(){
        String token = JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                () -> service.getUsername(token));
    }

    /**
     * Tests that when someone generates a JWT with an algorithm different to
     * ours the verification rejects the token as the signature is not verified.
     */
    @Test
    public void testResetPasswordJWTNotGeneratedByUs() {
        String token =
                JWT.create().withClaim("RESET_PASSWORD_EMAIL", "UserA@junit.com").sign(Algorithm.HMAC256(
                        "NotTheRealSecret"));
        Assertions.assertThrows(SignatureVerificationException.class,
                () -> service.getResetPasswordEmail(token));
    }

    /**
     * Tests that when a JWT token is generated if it does not contain us as
     * the issuer we reject it.
     */
    @Test
    public void testResetPasswordJWTCorrectlySignedNoIssuer() {
        String token =
                JWT.create().withClaim("RESET_PASSWORD_EMAIL", "UserA@junit.com")
                        .sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                () -> service.getResetPasswordEmail(token));
    }

    /**
     * Tests the password reset generation and verification.
     */
    @Test
    public void testPasswordResetToken() {
        LocalUser user = dao.findByUsernameIgnoreCase("UserA").get();
        String token = service.generatePasswordResetJWT(user);
        Assertions.assertEquals(user.getEmail(),
                service.getResetPasswordEmail(token), "Email should match inside " +
                        "JWT.");
    }
}
