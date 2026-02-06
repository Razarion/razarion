package com.btxtech.client;

import com.btxtech.client.rest.TeaVMRestAccess;
import com.btxtech.uiservice.ServerQuestProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMClientServerQuestProvider implements ServerQuestProvider {

    @Inject
    public TeaVMClientServerQuestProvider() {
    }

    @Override
    public void activateNextPossibleQuest() {
        TeaVMRestAccess.postNoBody("/rest/quest-controller/activateNextPossibleQuest", null, null);
    }
}
