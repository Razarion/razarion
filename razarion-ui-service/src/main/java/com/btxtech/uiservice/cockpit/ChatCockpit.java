package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.EmotePacket;

public interface ChatCockpit {
    void onMessage(ChatMessage chatMessage);

    /** A sign over somebody's unit - see EmotePacket. Shown by the Angular side over the unit. */
    default void onEmote(EmotePacket emotePacket) {
    }
}
