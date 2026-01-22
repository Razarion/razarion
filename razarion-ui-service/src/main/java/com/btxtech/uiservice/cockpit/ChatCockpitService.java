package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatCockpitService {
    private ChatCockpit chatCockpit;

    @Inject
    public ChatCockpitService() {
    }

    public void init(ChatCockpit chatCockpit) {
        this.chatCockpit = chatCockpit;
    }

    public void onMessage(ChatMessage chatMessage) {
        chatCockpit.onMessage(chatMessage);
    }
}
