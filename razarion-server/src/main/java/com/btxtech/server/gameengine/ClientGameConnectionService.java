package com.btxtech.server.gameengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service
public class ClientGameConnectionService extends TextWebSocketHandler {
    private static final String CLIENT_GAME_CONNECTION = "ClientGameConnection";
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnectionService.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var clientGameConnection = new ClientGameConnection();
        session.getAttributes().put(CLIENT_GAME_CONNECTION, clientGameConnection);
        clientGameConnection.onOpen(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        var clientGameConnection = (ClientGameConnection) session.getAttributes().get(CLIENT_GAME_CONNECTION);
        clientGameConnection.handleMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.warn("handleTransportError. Session: {}", session, exception);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        // TODO clientGameConnectionService.onClose(this);
        // TODO async = null;
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
