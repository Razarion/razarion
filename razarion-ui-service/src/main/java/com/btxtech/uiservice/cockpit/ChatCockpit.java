package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;
public interface ChatCockpit {
    void onMessage(ChatMessage chatMessage);
}
