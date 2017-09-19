package com.btxtech.server.connection;

import com.btxtech.server.gameengine.WebSocketEndpointConfigAware;
import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;

/**
 * Created by Beat
 * 25.04.2017.
 */
@ServerEndpoint(value = RestUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT, configurator = WebSocketEndpointConfigAware.class)
public class ClientSystemConnection {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private SessionService sessionService;
    private ObjectMapper mapper = new ObjectMapper();
    private RemoteEndpoint.Async async;
    private Date time;
    private String gameSessionUuid;
    private String httpSessionId;

    @OnMessage
    public void onMessage(Session session, String text) {
        try {
            SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            exceptionHandler.handleException("text: " + text, t);
        }
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        time = new Date();
        async = session.getAsyncRemote();
        httpSessionId = ((HttpSession) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY)).getId() ;
        clientSystemConnectionService.onOpen(this);
    }

    @OnError
    public void error(Session session, Throwable error) {
        System.out.println("************** ClientSystemConnection  error: " + error);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        clientSystemConnectionService.onClose(this);
        async = null;
    }

    private void onPackageReceived(SystemConnectionPacket packet, Object param) {
        switch (packet) {
            case LEVEL_UPDATE_CLIENT:
                serverLevelQuestService.onClientLevelUpdate(httpSessionId, (int) param);
                break;
            case SET_GAME_SESSION_UUID:
                gameSessionUuid = (String)param;
                break;
            default:
                throw new IllegalArgumentException("ClientSystemConnection Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
    }

    public PlayerSession getSession() {
        return sessionService.getSession(httpSessionId);
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
}
