package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsMessageEvent;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.system.TeaVMWebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.cockpit.ChatCockpitService;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMClientServerSystemConnection extends AbstractServerSystemConnection {
    private final TeaVMLifecycleService lifecycleService;
    private final TeaVMWebSocketWrapper webSocketWrapper;

    @Inject
    public TeaVMClientServerSystemConnection(TeaVMWebSocketWrapper webSocketWrapper,
                                             TeaVMLifecycleService lifecycleService,
                                             Boot boot,
                                             ChatCockpitService chatUiService,
                                             InventoryUiService inventoryUiService,
                                             UserUiService userUiService,
                                             GameUiControl gameUiControl) {
        super(boot, chatUiService, inventoryUiService, userUiService, gameUiControl);
        this.webSocketWrapper = webSocketWrapper;
        this.lifecycleService = lifecycleService;
    }

    @Override
    public void init() {
        String wsUrl = JsWindow.getWebSocketProtocol() + "//" + JsWindow.getLocationHost()
                + CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT
                + JwtHelper.bearerTokenToUrl(JwtHelper.getBearerTokenFromLocalStorage());
        webSocketWrapper.start(wsUrl,
                this::sendGameSessionUuid,
                this::handleMessage,
                lifecycleService::handleServerRestart,
                () -> lifecycleService.onConnectionLost("ClientServerSystemConnection"));
    }

    private void handleMessage(JsMessageEvent messageEvent) {
        try {
            String data = messageEvent.getDataAsString();
            handleMessage(data);
        } catch (Throwable throwable) {
            JsConsole.error("ClientServerSystemConnection.handleMessage() failed: " + throwable.getMessage());
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
    }

    @Override
    protected String toJson(Object param) {
        // TODO: implement proper JSON serialization for TeaVM
        if (param instanceof String) {
            return "\"" + param + "\"";
        } else if (param instanceof Integer) {
            return param.toString();
        }
        return String.valueOf(param);
    }

    @Override
    protected Object fromJson(String jsonString, SystemConnectionPacket packet) {
        if (packet.getTheClass() == Void.class) {
            return null;
        }
        // TODO: implement proper JSON deserialization for TeaVM
        // Needs TeaVMWorkerMarshaller-style deserialization for each packet type
        JsConsole.warn("fromJson not fully implemented for packet: " + packet.name());
        return null;
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
