package com.btxtech.server.gameengine;

import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
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
    private final UserService userService;
    private final UserActivityService userActivityService;
    private Date time;
    private String gameSessionUuid;
    private WebSocketSession wsSession;
    private String userId;
    private Date lastMessageSent;
    private Date lastMessageReceived;
    /**
     * Taken once at the handshake rather than read from the session later: a closing connection is
     * exactly when this is most worth having, and by then the session may no longer say.
     */
    private String userAgent;

    public ClientSystemConnection(UserService userService,
                                  UserActivityService userActivityService) {
        this.userService = userService;
        this.userActivityService = userActivityService;
    }

    public void init(WebSocketSession wsSession, String userId) {
        this.wsSession = wsSession;
        time = new Date();
        this.userId = userId;
        userAgent = wsSession.getHandshakeHeaders().getFirst(HttpHeaders.USER_AGENT);
    }

    public void handleMessage(WebSocketMessage<?> message) {
        try {
            lastMessageReceived = new Date();
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
            case SET_GAME_SESSION_UUID:
                String newGameSessionUuid = (String) param;
                // A warm restart sends a fresh uuid on the same connection, which is a new session
                // and deserves its own row. The same uuid twice is not.
                if (newGameSessionUuid != null && !newGameSessionUuid.equals(gameSessionUuid)) {
                    userActivityService.onGameSessionStarted(userId, newGameSessionUuid);
                }
                gameSessionUuid = newGameSessionUuid;
                break;
            default:
                throw new IllegalArgumentException("ClientSystemConnection Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) throws IOException {
        lastMessageSent = new Date();
        wsSession.sendMessage(new TextMessage(text));
    }

    public Date getTime() {
        return time;
    }

    public int getDuration() {
        return (int) (System.currentTimeMillis() - time.getTime());
    }

    public String getUserId() {
        return userId;
    }

    public Date getLastMessageSent() {
        return lastMessageSent;
    }

    public Date getLastMessageReceived() {
        return lastMessageReceived;
    }

    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public String toString() {
        return "ClientSystemConnection{" +
                "time=" + time +
                ", gameSessionUuid='" + gameSessionUuid + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
