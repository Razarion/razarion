package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;

public abstract class AbstractServerSystemConnection {
    private final GameUiControl gameUiControl;
    private final UserUiService userUiService;
    private final InventoryUiService inventoryUiService;
    private final ChatUiService chatUiService;
    private final Boot boot;

    public AbstractServerSystemConnection(Boot boot, ChatUiService chatUiService, InventoryUiService inventoryUiService, UserUiService userUiService, GameUiControl gameUiControl) {
        this.boot = boot;
        this.chatUiService = chatUiService;
        this.inventoryUiService = inventoryUiService;
        this.userUiService = userUiService;
        this.gameUiControl = gameUiControl;
    }

    protected abstract void sendToServer(String text);

    protected abstract void onLifecyclePacket(LifecyclePacket lifecyclePacket);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, SystemConnectionPacket packet);

    public abstract void init();

    public abstract void close();

    protected void openCallback() {
        chatUiService.clear();
    }

    public void sendGameSessionUuid() {
        sendToServer(ConnectionMarshaller.marshall(SystemConnectionPacket.SET_GAME_SESSION_UUID, toJson(boot.getGameSessionUuid())));
    }

    public void onLevelChanged(LevelConfig levelConfig) {
        sendToServer(ConnectionMarshaller.marshall(SystemConnectionPacket.LEVEL_UPDATE_CLIENT, toJson(levelConfig.getId())));
    }

    public void sendChatMessage(String message) {
        sendToServer(ConnectionMarshaller.marshall(SystemConnectionPacket.CHAT_SEND_MESSAGE, toJson(message)));
    }

    public void handleMessage(String text) {
        SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, packet);
        switch (packet) {
            case QUEST_PROGRESS_CHANGED:
                gameUiControl.onQuestProgress((QuestProgressInfo) param, true);
                break;
            case QUEST_ACTIVATED:
                gameUiControl.onQuestActivatedServer((QuestConfig) param);
                break;
            case QUEST_PASSED:
                gameUiControl.onQuestPassedServer((QuestConfig) param);
                break;
            case LEVEL_UPDATE_SERVER:
                userUiService.onServerLevelChange((LevelUpPacket) param);
                break;
            case XP_CHANGED:
                userUiService.onServerXpChange((Integer) param);
                break;
            case BOX_PICKED:
                inventoryUiService.onOnBoxPicked((BoxContent) param);
                break;
            case UNLOCKED_ITEM_LIMIT:
                userUiService.onUnlockItemLimitChanged((UnlockedItemPacket) param);
                break;
            case LIFECYCLE_CONTROL:
                onLifecyclePacket((LifecyclePacket) param);
                break;
            case CHAT_RECEIVE_MESSAGE:
                chatUiService.onMessage((ChatMessage) param);
                break;
            case EMAIL_VERIFIED:
                userUiService.onEmailVerified();
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

}
