package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.cockpit.ChatCockpit;

import java.util.List;

/**
 * Created by Beat
 * on 29.12.2017.
 */
public class DevToolChatCockpit implements ChatCockpit {
    @Override
    public void show() {
        System.out.println("+++++++ DevToolChatCockpit.show()");
    }

    @Override
    public void displayMessages(List<ChatMessage> messages) {
        System.out.println("-------------------------------------------------------");
        for (ChatMessage message : messages) {
            System.out.println("message: " + message);
        }
        System.out.println("-------------------------------------------------------");
    }
}
