package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsJson;
import com.btxtech.client.jso.JsMessageEvent;
import com.btxtech.client.jso.JsObject;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.rest.JsonDeserializer;
import com.btxtech.client.system.TeaVMWebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
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
        if (jsonString == null || jsonString.isEmpty() || "null".equals(jsonString)) {
            return null;
        }
        Class<?> type = packet.getTheClass();
        if (type == String.class) {
            if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                return jsonString.substring(1, jsonString.length() - 1);
            }
            return jsonString;
        }
        if (type == Integer.class) {
            return Integer.valueOf(jsonString);
        }
        JsObject obj = JsJson.parseObject(jsonString);
        if (type == QuestConfig.class) {
            return JsonDeserializer.deserializeQuestConfig(obj);
        }
        if (type == QuestProgressInfo.class) {
            return JsonDeserializer.deserializeQuestProgressInfo(obj);
        }
        if (type == LifecyclePacket.class) {
            return JsonDeserializer.deserializeLifecyclePacket(obj);
        }
        if (type == LevelUpPacket.class) {
            return JsonDeserializer.deserializeLevelUpPacket(obj);
        }
        if (type == BoxContent.class) {
            return JsonDeserializer.deserializeBoxContent(obj);
        }
        if (type == UnlockedItemPacket.class) {
            return JsonDeserializer.deserializeUnlockedItemPacket(obj);
        }
        if (type == ChatMessage.class) {
            return JsonDeserializer.deserializeChatMessage(obj);
        }
        JsConsole.warn("fromJson: unsupported packet type: " + packet.name());
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
