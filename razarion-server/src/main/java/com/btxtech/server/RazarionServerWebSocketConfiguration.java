package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class RazarionServerWebSocketConfiguration implements WebSocketConfigurer {
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final ClientGameConnectionService clientGameConnectionService;

    public RazarionServerWebSocketConfiguration(ClientSystemConnectionService clientSystemConnectionService,
                                                ClientGameConnectionService clientGameConnectionService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.clientGameConnectionService = clientGameConnectionService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientGameConnectionService, "/gameconnection")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");


        registry.addHandler(clientSystemConnectionService, "/systemconnection")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
