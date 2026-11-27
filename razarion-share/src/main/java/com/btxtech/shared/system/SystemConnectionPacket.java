package com.btxtech.shared.system;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.EmotePacket;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum SystemConnectionPacket implements ConnectionMarshaller.Packet {
    // Do not use Collections with generic types as top level parameter e.g. Map<Integer, Integer> List<Double>

    // System
    SET_GAME_SESSION_UUID(String.class),
    LIFECYCLE_CONTROL(LifecyclePacket.class),
    // Levels
    LEVEL_UPDATE_SERVER(LevelUpPacket.class),
    // Quest
    QUEST_ACTIVATED(QuestConfig.class),
    QUEST_PASSED(QuestConfig.class),
    QUEST_PROGRESS_CHANGED(QuestProgressInfo.class),
    ALL_QUESTS_COMPLETED(Void.class),
    // XP
    XP_CHANGED(Integer.class),
    // Inventory
    BOX_PICKED(BoxContent.class),
    // Unlock
    UNLOCKED_ITEM_LIMIT(UnlockedItemPacket.class),
    // Build: the master refused to build this item type at the chosen spot
    BUILD_REJECTED(Integer.class),
    // Chat
    CHAT_RECEIVE_MESSAGE(ChatMessage.class),
    // User
    EMAIL_VERIFIED(Void.class),
    // Emote over a unit
    EMOTE(EmotePacket.class);

    private final Class theClass;

    SystemConnectionPacket(Class theClass) {
        this.theClass = theClass;
    }

    @Override
    public Class getTheClass() {
        return theClass;
    }

}
