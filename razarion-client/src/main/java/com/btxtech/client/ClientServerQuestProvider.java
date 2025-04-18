package com.btxtech.client;

import com.btxtech.shared.rest.QuestControllerFactory;
import com.btxtech.uiservice.ServerQuestProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ClientServerQuestProvider implements ServerQuestProvider {
    private final Logger logger = Logger.getLogger(ClientServerQuestProvider.class.getName());

    @Inject
    public ClientServerQuestProvider() {
    }

    @Override
    public void activateNextPossibleQuest() {
        QuestControllerFactory.INSTANCE
                .activateNextPossibleQuest()
                .onFailed(fail -> logger.log(Level.SEVERE, "QuestProvider.activateNextPossibleQuest() failed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }
}
