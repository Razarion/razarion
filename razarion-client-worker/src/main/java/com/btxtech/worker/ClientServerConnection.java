package com.btxtech.worker;

import com.btxtech.shared.gameengine.planet.connection.AbstractServerConnection;
import com.btxtech.shared.rest.RestUrl;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.WebSocket;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

import javax.enterprise.context.Dependent;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.04.2017.
 */
@Dependent
public class ClientServerConnection extends AbstractServerConnection {
    private Logger logger = Logger.getLogger(ClientServerConnection.class.getName());
    private WebSocket webSocket;

    @Override
    public void init() {
        String wsProtocol;
        if (Browser.getWindow().getLocation().getProtocol().equals("https:")) {
            wsProtocol = "wss";
        } else {
            wsProtocol = "ws";
        }
        String port;
        if (Browser.getWindow().getLocation().getPort() == null || Browser.getWindow().getLocation().getPort().trim().isEmpty()) {
            port = "";
        } else {
            port = ":" + Browser.getWindow().getLocation().getPort();
        }

        String url = wsProtocol + "://" + Browser.getWindow().getLocation().getHostname() + port + RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT;

        logger.severe("WS URL: " + url);

        webSocket = Browser.getWindow().newWebSocket(url);
        webSocket.setOnerror(evt -> logger.severe("WebSocket OnError: " + evt));
        webSocket.setOnclose(evt -> logger.severe("WebSocket Close: " + evt));
        webSocket.setOnopen(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                logger.severe("WebSocket Open");
            }
        });
    }

    @Override
    protected void sendToServer(Package aPackage, Object param) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aPackage.name());
        stringBuilder.append("#");
        if(param != null) {
            stringBuilder.append(MarshallingWrapper.toJSON(param));
        }
        webSocket.send(stringBuilder.toString());
    }
}
