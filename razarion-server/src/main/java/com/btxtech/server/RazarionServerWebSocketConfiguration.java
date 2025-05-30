package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class RazarionServerWebSocketConfiguration implements WebSocketConfigurer {
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
}
