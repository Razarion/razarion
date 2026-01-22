package com.btxtech.client;

import com.btxtech.client.rest.DominoRestAccess;
import com.btxtech.uiservice.ServerQuestProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
