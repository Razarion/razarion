package com.btxtech.uiservice.cockpit;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 28.12.2017.
 */
@Singleton
public class ChatService {
    @Inject
    private Instance<ChatCockpit> instance;
    private ChatCockpit chatCockpit;

    public void start() {
        if (chatCockpit == null) {
            chatCockpit = instance.get();
            chatCockpit.show();
        }
    }

}
