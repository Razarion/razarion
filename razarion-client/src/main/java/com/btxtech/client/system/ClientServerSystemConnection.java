package com.btxtech.client.system;

import com.btxtech.common.system.WebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import elemental2.dom.Event;
import elemental2.dom.MessageEvent;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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
    @Inject
    private WebSocketWrapper webSocketWrapper;

    @Override
    public void init() {
        webSocketWrapper.start(CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT,
                () -> {
                    openCallback();
                    sendGameSessionUuid();
                }, this::handleMessage, lifecycleService::handleServerRestart,
                () -> lifecycleService.onConnectionLost("ClientServerSystemConnection"));
    }


    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.data);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerGameConnection.handleMessage() failed", throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
    }

    @Override
    protected String toJson(Object param) {
        // return MarshallingWrapper.toJSON(param);
        return null;
    }

    @Override
    protected Object fromJson(String jsonString, SystemConnectionPacket packet) {
        if (packet.getTheClass() == Void.class) {
            return null;
        } else {
            // return MarshallingWrapper.fromJSON(jsonString, packet.getTheClass());
            return null;
        }
    }

    @Override
    public void close() {
        webSocketWrapper.close();
    }

    @Override
    protected void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        lifecycleService.onLifecyclePacket(lifecyclePacket);
    }
}
