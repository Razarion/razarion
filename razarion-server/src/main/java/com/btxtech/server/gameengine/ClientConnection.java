package com.btxtech.server.gameengine;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerConnection;
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
    private ObjectMapper mapper = new ObjectMapper();
    private EndpointConfig config;

    @OnMessage
    public void onMessage(javax.websocket.Session session, String msg) {
        try {
            int delimiterOffset = msg.indexOf(AbstractServerConnection.PACKAGE_DELIMITER);
            if (delimiterOffset < 0) {
                throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + msg);
            }
            AbstractServerConnection.Package aPackage = AbstractServerConnection.Package.valueOf(msg.substring(0, delimiterOffset));
            System.out.println("Package: " + aPackage);
            Object param = mapper.readValue(msg.substring(delimiterOffset + 1), aPackage.getTheClass());
            onPackageReceived(aPackage, param);
            System.out.println("Param: " + param + " " + param.getClass());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @OnOpen
    public void open(javax.websocket.Session session, EndpointConfig config) {
        this.config = config;
        System.out.println("**************   open: " + config);
    }

    @OnError
    public void error(javax.websocket.Session session, Throwable error) {
        System.out.println("**************   error: " + error);
    }

    @OnClose
    public void close(javax.websocket.Session session, CloseReason reason) {
        System.out.println("**************   close: " + reason);
    }

    protected void onPackageReceived(AbstractServerConnection.Package aPackage, Object param) {
        UserContext userContext = getUser();
        switch (aPackage) {
            case CREATE_BASE:
                baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), (int) userContext.getUserId(), userContext.getName(), (DecimalPosition) param);
                break;
        }
    }

    private UserContext getUser() {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(WebSocketEndpointConfigAware.HTTP_SESSION_KEY);
        return userService.getLoggedInUser(httpSession.getId());
    }
}
