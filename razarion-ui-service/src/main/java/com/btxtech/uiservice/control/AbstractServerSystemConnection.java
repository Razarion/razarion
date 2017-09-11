package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public abstract class AbstractServerSystemConnection {
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private UserUiService userUiService;

    protected abstract void sendToServer(String text);

    protected abstract void onLifecyclePacket(LifecyclePacket lifecyclePacket);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, SystemConnectionPacket packet);

    public abstract void init();

    public abstract void close();

    public void onLevelChanged(LevelConfig levelConfig) {
        sendToServer(ConnectionMarshaller.marshall(SystemConnectionPacket.LEVEL_UPDATE_CLIENT, toJson(levelConfig.getLevelId())));
    }

    public void handleMessage(String text) {
        SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, packet);
        switch (packet) {
            case QUEST_PROGRESS_CHANGED:
                gameUiControl.onQuestProgress((QuestProgressInfo) param);
                break;
            case QUEST_ACTIVATED:
                gameUiControl.onQuestActivated((QuestConfig) param);
                break;
            case QUEST_PASSED:
                gameUiControl.onQuestPassedServer((QuestConfig) param);
                break;
            case LEVEL_UPDATE_SERVER:
                userUiService.onServerLevelChange((UserContext) param);
                break;
            case XP_CHANGED:
                userUiService.onServerXpChange((Integer) param);
                break;
            case LIFECYCLE_CONTROL:
                onLifecyclePacket((LifecyclePacket) param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

}
