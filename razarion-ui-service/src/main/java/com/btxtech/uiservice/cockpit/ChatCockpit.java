package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;

import java.util.List;

/**
 * Created by Beat
 * on 28.12.2017.
 */
public interface ChatCockpit {
    void show();

    void displayMessages(List<ChatMessage> messages);
}
