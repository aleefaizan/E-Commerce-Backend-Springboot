package com.myecommerceapp.espra.config;

import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;
import java.util.function.Supplier;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {


    @Autowired
    private ApplicationContext context;
    @Autowired
    private JWTRequestFilter filter;
    @Autowired
    private UserServiceImpl userService;
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").setAllowedOriginPatterns("**").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/apps");
    }

    private AuthorizationManager<Message<?>> makeMessageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messeages =
                new MessageMatcherDelegatingAuthorizationManager.Builder();
        messeages
                .simpDestMatchers("/topic/user/**")
                .authenticated()
                .anyMessage().permitAll();
        return messeages.build();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        AuthorizationManager<Message<?>> authorizationManager =  makeMessageAuthorizationManager();

        AuthorizationChannelInterceptor authorizationChannelInterceptor =
                new AuthorizationChannelInterceptor(authorizationManager);

        AuthorizationEventPublisher publisher =
                new SpringAuthorizationEventPublisher(context);

        authorizationChannelInterceptor.setAuthorizationEventPublisher(publisher);

        registration.interceptors(filter, authorizationChannelInterceptor,
                new RejectClientMessagesOnChannelsChannelInterceptor(), new DestinationLevelAuthorizationChannelInterceptor());
    }

    private class RejectClientMessagesOnChannelsChannelInterceptor implements ChannelInterceptor {

        private String[] paths = {"/topic/user/**/address"};
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.MESSAGE)) {
                String destination = (String) message.getHeaders().get("simpDestination");
                for (String path: paths) {
                    if (MATCHER.match(path, destination)){
                        message = null;
                    }
                }
            }
            return message;
        }
    }

    private class DestinationLevelAuthorizationChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.SUBSCRIBE)) {
                String destination = (String) message.getHeaders().get("simpDestination");
                String userTopicMatcher = "/topic/user/{userId}/**";
                if (MATCHER.match(userTopicMatcher, destination)){
                    Map<String, String> params = MATCHER.extractUriTemplateVariables(userTopicMatcher, destination);
                    try {
                        Long userId = Long.valueOf(params.get("userId"));
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null) {
                            LocalUser user = (LocalUser) authentication.getPrincipal();
                            if (!userService.userHasPermissionToUser(user, userId)){
                                message = null;
                            }
                        } else {
                            message = null;
                        }
                    } catch (NumberFormatException ex) {
                        message = null;
                    }
                }
            }
            return message;
        }
    }
}
