package com.btxtech.uiservice.mock;

import com.btxtech.uiservice.ServerQuestProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

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
