package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class RazarionServerWebSocketConfiguration implements WebSocketConfigurer {
    // A* paths over big maps can produce >1k waypoints (one DecimalPosition per cell).
    // JSON-encoded that easily exceeds the Tomcat/Spring default of 8 KB, which closes
    // the socket with code 1009. 1 MB is plenty even for worst-case 100k-budget paths.
    private static final int MAX_TEXT_MESSAGE_BUFFER_SIZE = 1024 * 1024;
    private static final int MAX_BINARY_MESSAGE_BUFFER_SIZE = 1024 * 1024;

    private final ClientSystemConnectionService clientSystemConnectionService;
    private final ClientGameConnectionService clientGameConnectionService;
    private final JwtDecoder jwtDecoder;

    public RazarionServerWebSocketConfiguration(ClientSystemConnectionService clientSystemConnectionService,
                                                ClientGameConnectionService clientGameConnectionService,
                                                JwtDecoder jwtDecoder) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.clientGameConnectionService = clientGameConnectionService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientGameConnectionService, "/gameconnection")
                .addInterceptors(new HttpSessionHandshakeInterceptor(), new AuthHandshakeInterceptor(jwtDecoder))
                .setAllowedOrigins("*");


        registry.addHandler(clientSystemConnectionService, "/systemconnection")
                .addInterceptors(new HttpSessionHandshakeInterceptor(), new AuthHandshakeInterceptor(jwtDecoder))
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BUFFER_SIZE);
        container.setMaxBinaryMessageBufferSize(MAX_BINARY_MESSAGE_BUFFER_SIZE);
        return container;
    }
}
