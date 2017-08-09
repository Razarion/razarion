package com.btxtech.shared.system;

import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum SystemConnectionPacket implements ConnectionMarshaller.Packet {
    // Levels
    LEVEL_UPDATE(Integer.class),
    // Quest
    QUEST_PROGRESS_CHANGED(QuestProgressInfo.class);

    private Class theClass;

    SystemConnectionPacket(Class theClass) {
        this.theClass = theClass;
    }

    @Override
    public Class getTheClass() {
        return theClass;
    }

}
