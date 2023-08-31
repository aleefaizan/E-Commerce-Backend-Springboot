package com.myecommerceapp.espra.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.LocalUserDAO;
import com.myecommerceapp.espra.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter implements ChannelInterceptor {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDAO dao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");
        UsernamePasswordAuthenticationToken token = checkToken(tokenHeader);
        if (token != null) {
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken checkToken(String token){
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
            try {
                String username = jwtService.getUsername(token);
                Optional<LocalUser> optUser = dao.findByUsernameIgnoreCase(username);
                if (optUser.isPresent()){
                    LocalUser user = optUser.get();
                    if (user.getEmailVerified()) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        return authenticationToken;
                    }
                }
            } catch (JWTDecodeException ignored) {
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return null;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageType messageType = (SimpMessageType) message.getHeaders().get("simpMessagetype");
        if (messageType.equals(SimpMessageType.SUBSCRIBE)
                    || messageType.equals(SimpMessageType.MESSAGE)) {
            Map nativeHeaders = (Map) message.getHeaders().get("nativeHeaders");
            if (nativeHeaders != null) {
                List authTokenList = (List) nativeHeaders.get("Authorization");
                if (authTokenList != null) {
                    String tokenHeader = (String) authTokenList.get(0);
                    checkToken(tokenHeader);
                }
            }
        }
        return message;
    }
}
