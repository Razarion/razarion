package com.btxtech.server.connection;

import com.btxtech.server.gameengine.WebSocketEndpointConfigAware;
import com.btxtech.server.user.UserService;
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
    private UserService userService;
    private ObjectMapper mapper = new ObjectMapper();
    private EndpointConfig config;
    private RemoteEndpoint.Async async;

    @OnMessage
    public void onMessage(Session session, String text) {
        try {
            SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, packet.getTheClass());
            onPackageReceived(packet, param);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        this.config = config;
        async = session.getAsyncRemote();
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

    protected void onPackageReceived(SystemConnectionPacket packet, Object param) {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY);
        switch (packet) {
            case LEVEL_UPDATE:
                userService.onLevelUpdate(httpSession.getId(), (int) param);
                break;
            default:
                throw new IllegalArgumentException("ClientSystemConnection Unknown Packet: " + packet);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
    }
}
