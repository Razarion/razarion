package com.btxtech.uiservice.mock;

import com.btxtech.uiservice.ServerQuestProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TestServerQuestProvider implements ServerQuestProvider {

    @Inject
    public TestServerQuestProvider() {
    }

    @Override
    public void activateNextPossibleQuest() {
        System.out.println("TestServerQuestProvider.activateNextPossibleQuest()");
    }
}
