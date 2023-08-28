package com.myecommerceapp.espra.api.controller.auth;

import com.myecommerceapp.espra.api.model.LoginBody;
import com.myecommerceapp.espra.api.model.LoginResponse;
import com.myecommerceapp.espra.api.model.RegistrationBody;
import com.myecommerceapp.espra.exception.UserAlreadyExistsException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.service.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/register")
    public ResponseEntity registerUser(@Valid @RequestBody RegistrationBody registrationBody) throws UserAlreadyExistsException {
        try {
            userService.createUser(registrationBody);
            return ResponseEntity.ok().build();
        } catch (UserAlreadyExistsException ec) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody){
        String jwt = userService.loginUser(loginBody);
        if (jwt == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/me")
    public LocalUser getLoggedInUser(@AuthenticationPrincipal LocalUser user){
        return user;
    }
}
