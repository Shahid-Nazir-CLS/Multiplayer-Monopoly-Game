package com.game.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix for client-to-server messages
        // Messages sent to /app/sendMessage will be handled by application components (e.g., controllers).
        config.setApplicationDestinationPrefixes("/app"); 
        
        // // Prefixes for broker (server-to-client) messages
        // broker prefix, to which users subscribe to e.g. /user/music, /user/logs etc
        // message will be sent to all subscribers
        config.enableSimpleBroker("/topic", "/queue", "/user"); 

        // Prefix for routing messages to specific users
        // server to client prefix
        // configuration helps route messages to a specific user. 
        // This is used by the application to identify which user should receive the message, 
        // based on the destination.
        config.setUserDestinationPrefix("/user"); 
    }

    // Register WebSocket endpoint
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Add endpoint for WebSocket connections at /ws
        // SockJS is used to provide fallback options for browsers that don’t support WebSocket
        registry.addEndpoint("/ws").withSockJS();
    }

    // Configure message converters
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Set default content type to JSON
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        // Use Jackson to convert messages to/from JSON
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper()); // Use ObjectMapper for JSON conversion
        converter.setContentTypeResolver(resolver); // Set the content type resolver

        // Add the configured converter to the list of message converters
        messageConverters.add(converter);

        return false; // Return false to use the default message converters as well
    }
}