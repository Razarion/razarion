package com.btxtech.client.system;

import com.btxtech.common.WebSocketHelper;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Dependent
public class ClientServerSystemConnection extends AbstractServerSystemConnection {
    @Inject
    private LifecycleService lifecycleService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Logger logger = Logger.getLogger(ClientServerSystemConnection.class.getName());
    private WebSocket webSocket;

    @Override
    public void init() {
        webSocket = Browser.getWindow().newWebSocket(WebSocketHelper.getUrl(CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT));
        webSocket.setOnerror(evt -> logger.severe("ClientServerSystemConnection WebSocket OnError: " + evt));
        webSocket.setOnclose(evt -> logger.severe("ClientServerSystemConnection WebSocket Close: " + evt));
        webSocket.setOnmessage(this::handleMessage);
        webSocket.setOnopen(evt -> {
            sendGameSessionUuid();
        });
    }

    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.getData());
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerGameConnection.handleMessage() failed", throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocket.send(text);
    }

    @Override
    protected String toJson(Object param) {
        return MarshallingWrapper.toJSON(param);
    }

    @Override
    protected Object fromJson(String jsonString, SystemConnectionPacket packet) {
        return MarshallingWrapper.fromJSON(jsonString, packet.getTheClass());
    }

    @Override
    public void close() {
        try {
            webSocket.close();
            webSocket = null;
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerSystemConnection.close()", throwable);
        }
    }

    @Override
    protected void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        lifecycleService.onLifecyclePacket(lifecyclePacket);
    }
}
