package com.btxtech.server.gameengine;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;
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
 * 20.04.2017.
 */
@ServerEndpoint(value = RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT, configurator = WebSocketEndpointConfigAware.class)
public class ClientConnection {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private UserService userService;
    @Inject
    private ClientConnectionService clientConnectionService;
    private ObjectMapper mapper = new ObjectMapper();
    private EndpointConfig config;
    private RemoteEndpoint.Async async;

    @OnMessage
    public void onMessage(Session session, String text) {
        try {
            ConnectionMarshaller.Package aPackage = ConnectionMarshaller.deMarshallPackage(text);
            String payload = ConnectionMarshaller.deMarshallPayload(text);
            Object param = mapper.readValue(payload, aPackage.getTheClass());
            onPackageReceived(aPackage, param);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        this.config = config;
        async = session.getAsyncRemote();
        clientConnectionService.onOpen(this);
    }

    @OnError
    public void error(Session session, Throwable error) {
        System.out.println("**************   error: " + error);
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        clientConnectionService.onClose(this);
        async = null;
    }

    protected void onPackageReceived(ConnectionMarshaller.Package aPackage, Object param) {
        UserContext userContext = getUser();
        switch (aPackage) {
            case CREATE_BASE:
                baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), (int) userContext.getUserId(), userContext.getName(), (DecimalPosition) param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + aPackage);
        }
    }

    public void sendToClient(String text) {
        async.sendText(text);
    }


    private UserContext getUser() {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY);
        return userService.getLoggedInUser(httpSession.getId());
    }
}
