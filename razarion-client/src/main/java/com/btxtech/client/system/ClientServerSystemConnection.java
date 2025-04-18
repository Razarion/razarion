package com.btxtech.client.system;

import com.btxtech.common.WorkerMarshaller;
import com.btxtech.common.system.WebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;
import elemental2.dom.Event;
import elemental2.dom.MessageEvent;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.04.2017.
 */

public class ClientServerSystemConnection extends AbstractServerSystemConnection {
    private final Logger logger = Logger.getLogger(ClientServerSystemConnection.class.getName());
    private final LifecycleService lifecycleService;
    private final WebSocketWrapper webSocketWrapper;

    @Inject
    public ClientServerSystemConnection(WebSocketWrapper webSocketWrapper,
                                        LifecycleService lifecycleService,
                                        Boot boot,
                                        ChatUiService chatUiService,
                                        InventoryUiService inventoryUiService,
                                        UserUiService userUiService,
                                        GameUiControl gameUiControl) {
        super(boot, chatUiService, inventoryUiService, userUiService, gameUiControl);
        this.webSocketWrapper = webSocketWrapper;
        this.lifecycleService = lifecycleService;
    }

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
            logger.log(Level.SEVERE, "ClientServerGameConnection.handleMessage() failed: " + ((MessageEvent) event).data, throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
    }

    @Override
    protected String toJson(Object param) {
        return WorkerMarshaller.toJson(param);
    }

    @Override
    protected Object fromJson(String jsonString, SystemConnectionPacket packet) {
        if (packet.getTheClass() == Void.class) {
            return null;
        } else {
            return WorkerMarshaller.fromJson(jsonString, packet.getTheClass());
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
