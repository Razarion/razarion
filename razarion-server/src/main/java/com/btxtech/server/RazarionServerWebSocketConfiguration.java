package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RazarionServerWebSocketConfiguration implements WebSocketConfigurer {
    private final ClientGameConnectionService clientGameConnectionService;

    public RazarionServerWebSocketConfiguration(ClientGameConnectionService clientGameConnectionService) {
        this.clientGameConnectionService = clientGameConnectionService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientGameConnectionService, "/gameconnection")
                .setAllowedOrigins("*");

        registry.addHandler(new SimpleWebSocketHandler("systemconnection"), "/systemconnection");
    }


    private static class SimpleWebSocketHandler implements WebSocketHandler {
        private final String path;
        private final Logger logger = LoggerFactory.getLogger(SimpleWebSocketHandler.class);

        public SimpleWebSocketHandler(String path) {
            this.path = path;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.info("Established connection '{}'", path);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            logger.info("handleMessage '{}' message: '{}' session: '{}'", path, message, session);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            logger.info("handleTransportError '{}'", path);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            logger.info("afterConnectionClosed '{}'", path);
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}
