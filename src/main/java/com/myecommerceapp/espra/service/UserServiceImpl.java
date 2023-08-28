package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.api.model.LoginBody;
import com.myecommerceapp.espra.api.model.RegistrationBody;
import com.myecommerceapp.espra.exception.UserAlreadyExistsException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl {

    @Autowired
    private LocalUserDAO dao;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    public RegistrationBody createUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if (dao.findByUsernameIgnoreCase((registrationBody.getUsername())).isPresent()
                || dao.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()){
            throw new UserAlreadyExistsException();
        }
        LocalUser user = localUser(registrationBody);
        dao.save(user);
        return registrationBody(user);
    }

    public String loginUser(LoginBody loginBody){
        Optional<LocalUser> optUser = dao.findByUsernameIgnoreCase(loginBody.getUsername());

        if (optUser.isPresent()){
            LocalUser user = optUser.get();
            if (passwordEncoder.matches(loginBody.getPassword(), user.getPassword())){
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }

    private LocalUser localUser(RegistrationBody registrationBody){

        LocalUser user = mapper.map(registrationBody, LocalUser.class);
        //Encrypting the password using BCrypt Password Encoder.
        user.setPassword(passwordEncoder.encode(registrationBody.getPassword()));

        return user;
    }
    private RegistrationBody registrationBody(LocalUser user){
        return mapper.map(user, RegistrationBody.class);
    }
}
