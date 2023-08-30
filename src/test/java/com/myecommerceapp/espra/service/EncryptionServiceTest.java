package com.myecommerceapp.espra.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class EncryptionServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncryption(){
        String password = "PasswordIsSecret!123";
        String hash = passwordEncoder.encode(password);
        Assertions.assertTrue(passwordEncoder.matches(password, hash), "Hash password should matches original");
        Assertions.assertFalse(passwordEncoder.matches(password + "Bad", hash), "Altered password should not be valid");
    }
}
