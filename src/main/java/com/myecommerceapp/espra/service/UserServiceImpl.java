package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.api.model.LoginBody;
import com.myecommerceapp.espra.api.model.PasswordResetBody;
import com.myecommerceapp.espra.api.model.RegistrationBody;
import com.myecommerceapp.espra.exception.EmailFailureException;
import com.myecommerceapp.espra.exception.EmailNotFoundException;
import com.myecommerceapp.espra.exception.UserAlreadyExistsException;
import com.myecommerceapp.espra.exception.UserNotVerifiedException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.VerificationToken;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import com.myecommerceapp.espra.model.dao.VerificationTokenDAO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
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

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> optUser = dao.findByUsernameIgnoreCase(loginBody.getUsername());

        if (optUser.isPresent()){
            LocalUser user = optUser.get();
            if (passwordEncoder.matches(loginBody.getPassword(), user.getPassword())){
                if (user.getEmailVerified()) {
                    return jwtService.generateJWT(user);
                } else {
                    List<VerificationToken> tokenList = user.getVerificationTokens();
                    boolean resend =  tokenList.size() == 0 ||
                            tokenList.get(0).getCreatedTimeStamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
                    if (resend){
                        VerificationToken token = createVerificationToken(user);
                        verificationTokenDAO.save(token);
                        service.sendVerificationEmail(token);
                    }
                    throw new UserNotVerifiedException(resend);
                }
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

    @Transactional
    public boolean verifyUser(String token){
        Optional<VerificationToken> optToken = verificationTokenDAO.findByToken(token);
        if (optToken.isPresent()){
            VerificationToken verificationToken = optToken.get();
            LocalUser user = verificationToken.getLocalUser();
            if (!user.getEmailVerified()) {
                user.setEmailVerified(true);
                dao.save(user);
                verificationTokenDAO.deleteByLocalUser(user);
                return true;
            }
        }
        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> optUser = dao.findByEmailIgnoreCase(email);
        if (optUser.isPresent()){
            LocalUser user = optUser.get();
            String token = jwtService.generatePasswordResetJWT(user);
            service.sendPasswordEmail(user, token);
        } else {
            throw new EmailNotFoundException();
        }
    }

    public void resetPassword(PasswordResetBody body){
        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional<LocalUser> optUser = dao.findByEmailIgnoreCase(email);
        if (optUser.isPresent()){
            LocalUser user = optUser.get();
            user.setPassword(passwordEncoder.encode(body.getPassword()));
            dao.save(user);
        }
    }

    public boolean userHasPermissionToUser(LocalUser user, Long id) {
        return Objects.equals(user.getId(), id);
    }
}
