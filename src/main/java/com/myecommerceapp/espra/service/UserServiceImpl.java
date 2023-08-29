package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.api.model.LoginBody;
import com.myecommerceapp.espra.api.model.RegistrationBody;
import com.myecommerceapp.espra.exception.EmailFailureException;
import com.myecommerceapp.espra.exception.UserAlreadyExistsException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.VerificationToken;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import com.myecommerceapp.espra.model.dao.VerificationTokenDAO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
    private EmailService service;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private VerificationTokenDAO verificationTokenDAO;

    public RegistrationBody createUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if (dao.findByUsernameIgnoreCase((registrationBody.getUsername())).isPresent()
                || dao.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()){
            throw new UserAlreadyExistsException();
        }
        LocalUser user = localUser(registrationBody);

        VerificationToken verificationToken = createVerificationToken(user);
        service.sendVerificationEmail(verificationToken);
        verificationTokenDAO.save(verificationToken);
        dao.save(user);
        return registrationBody(user);
    }

    private VerificationToken createVerificationToken(LocalUser user){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimeStamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setLocalUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
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
