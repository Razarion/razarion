package com.btxtech.server.gameengine;

import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;

@Component
@Scope("prototype")
public class ClientSystemConnection {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ClientSystemConnection.class);
    private final ServerLevelQuestService serverLevelQuestService;
    private final SessionService sessionService;
    private Date time;
    private String gameSessionUuid;
    private String httpSessionId;
    private WebSocketSession wsSession;

    public ClientSystemConnection(ServerLevelQuestService serverLevelQuestService, SessionService sessionService) {
        this.serverLevelQuestService = serverLevelQuestService;
        this.sessionService = sessionService;
    }

    public void init(WebSocketSession wsSession, String httpSessionId) {
        this.wsSession = wsSession;
        this.httpSessionId = httpSessionId;
        time = new Date();
        // TODO chatPersistence.sendLastMessages(getSession());
    }

    public void handleMessage(WebSocketMessage<?> message) {
        try {
            var text = message.getPayload().toString();
            SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = MAPPER.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            logger.warn("message: {} session: {}", message, wsSession, t);
        }
    }

    private void onPackageReceived(SystemConnectionPacket packet, Object param) {
        switch (packet) {
            case LEVEL_UPDATE_CLIENT:
                serverLevelQuestService.onClientLevelUpdate(httpSessionId, (int) param);
                break;
            case SET_GAME_SESSION_UUID:
                gameSessionUuid = (String) param;
                break;
            case CHAT_SEND_MESSAGE:
                // TODO chatPersistence.onMessage(getSession(), (String) param);
                // TODO break;
                throw new UnsupportedOperationException("... TODO ...");
            default:
                throw new IllegalArgumentException("ClientSystemConnection Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) throws IOException {
        wsSession.sendMessage(new TextMessage(text));
    }

    public PlayerSession getSession() {
        return sessionService.getSession(httpSessionId);
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public Date getTime() {
        return time;
    }

    public int getDuration() {
        return (int) (System.currentTimeMillis() - time.getTime());
    }

    @Override
    public String toString() {
        return "ClientSystemConnection{" +
                "time=" + time +
                ", gameSessionUuid='" + gameSessionUuid + '\'' +
                ", httpSessionId='" + httpSessionId + '\'' +
                '}';
    }
}
