package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 28.12.2017.
 */
@Singleton
public class ChatUiService {
    private static final int MESSAGE_COUNT = 100;
    private ChatCockpit chatCockpit;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Inject
    public ChatUiService() {
    }

    public void start() {
        if (chatCockpit == null) {
            // TODO chatCockpit = instance.get();
            // TODO chatCockpit.show();
            // TODO chatCockpit.displayMessages(chatMessages);
        }
    }

    public void onMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        if (chatMessages.size() > MESSAGE_COUNT) {
            chatMessages.remove(0);
        }
        if (chatCockpit != null) {
            chatCockpit.displayMessages(chatMessages);
        }
    }

    public void clear() {
        chatMessages.clear();
        if (chatCockpit != null) {
            chatCockpit.displayMessages(chatMessages);
        }
    }
}
