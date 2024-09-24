package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.ServerQuestProvider;

import javax.inject.Singleton;

@Singleton
public class TestServerQuestProvider implements ServerQuestProvider {
    @Override
    public void activateNextPossibleQuest() {
        System.out.println("TestServerQuestProvider.activateNextPossibleQuest()");
    }
}
