package com.myecommerceapp.espra.service;

import com.myecommerceapp.espra.exception.EmailFailureException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${email.from}")
    private String fromAddress;

    @Value("${website.url}")
    private String url;


    @Autowired
    private JavaMailSender javaMailSender;

    private SimpleMailMessage simpleMailMessage(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);

        return message;
    }

    public void sendVerificationEmail(VerificationToken token) throws EmailFailureException {

        SimpleMailMessage message = simpleMailMessage();
        message.setTo(token.getLocalUser().getEmail());
        message.setSubject("Account Verification");
        message.setText(
                "Hello " + token.getLocalUser().getFirstName() +
                "\n" +
                "Thank you for signing up with our service! To complete your registration, please click the link below to verify your email address:\n" +
                "\n" +
                url + "/auth/verify?token=" + token.getToken() +
                "\n" +
                "If you didn't sign up for an account, you can safely ignore this email.\n" +
                "\n" +
                "Best regards,\n" +
                "The ESPRA Team\n"
        );
        try {
            javaMailSender.send(message);
        } catch (MailException ex) {
            throw new EmailFailureException();
        }
    }

    public void sendPasswordEmail(LocalUser user, String token) throws EmailFailureException{
        SimpleMailMessage message = simpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Link");
        message.setText("Hello " + user.getFirstName() +
                "\n" +
                "You have requested to reset your password, please click the link below to reset your password:\n" +
                "\n" +
                url + "/auth/reset?token=" + token +
                "\n" +
                "This link will be expired in 30 minutes from now.\n" +
                "\n" +
                "Best regards,\n" +
                "The ESPRA Team\n");
        try {
            javaMailSender.send(message);
        } catch (MailException ex) {
            throw new EmailFailureException();
        }
    }

}
