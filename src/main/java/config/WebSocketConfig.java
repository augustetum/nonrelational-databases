package config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import service.JwtService;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/queue","/user"); //queue leidžia p2p, topic labiau notificationam / public announcements
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    System.out.println("CONNECT command received");
                    String token = extractToken(accessor);
                    System.out.println("Extracted token: " + (token != null ? "present" : "null"));

                    if (token != null) {
                        try {
                            String username = jwtService.extractUsername(token);
                            System.out.println("Extracted username: " + username);
                            if (username != null && !username.isEmpty()) {
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                                accessor.setUser(auth);
                                accessor.getSessionAttributes().put("username", username);
                            }
                        } catch (Exception e) {
                            System.err.println("Invalid JWT token: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("No token found in headers");
                    }
                } else if (StompCommand.MESSAGE.equals(accessor.getCommand()) ||
                           StompCommand.SEND.equals(accessor.getCommand()) ||
                           StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    // tolimesnem zinutem is sessiono useris
                    if (accessor.getSessionAttributes() != null) {
                        String username = (String) accessor.getSessionAttributes().get("username");
                        if (username != null) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                            accessor.setUser(auth);
                        } else {
                            System.err.println("Username is null in session");
                        }
                    } else {
                        System.err.println("Session attributes is null");
                    }
                }

                return message;
            }

            private String extractToken(StompHeaderAccessor accessor) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
                return null;
            }
        });
    }

}