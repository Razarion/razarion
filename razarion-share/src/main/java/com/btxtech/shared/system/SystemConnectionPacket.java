package com.btxtech.shared.system;

import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum SystemConnectionPacket implements ConnectionMarshaller.Packet {
    // System
    SET_GAME_SESSION_UUID(String.class),
    LIFECYCLE_CONTROL(LifecyclePacket.class),
    // Levels
    LEVEL_UPDATE_CLIENT(Integer.class),
    LEVEL_UPDATE_SERVER(UserContext.class),
    // Quest
    QUEST_ACTIVATED(QuestConfig.class),
    QUEST_PASSED(QuestConfig.class),
    QUEST_PROGRESS_CHANGED(QuestProgressInfo.class),
    // XP
    XP_CHANGED(Integer.class);

    private Class theClass;

    SystemConnectionPacket(Class theClass) {
        this.theClass = theClass;
    }

    @Override
    public Class getTheClass() {
        return theClass;
    }

}
