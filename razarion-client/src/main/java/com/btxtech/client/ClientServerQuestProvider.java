package com.btxtech.client;

import com.btxtech.client.rest.DominoRestAccess;
import com.btxtech.uiservice.ServerQuestProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClientServerQuestProvider implements ServerQuestProvider {
    @Inject
    public ClientServerQuestProvider() {
    }

    @Override
    public void activateNextPossibleQuest() {
        DominoRestAccess.getQuestAccess().activateNextPossibleQuest();
    }
}
