package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.ServerQuestProvider;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestServerQuestProvider implements ServerQuestProvider {
    @Override
    public void activateNextPossibleQuest() {
        System.out.println("TestServerQuestProvider.activateNextPossibleQuest()");
    }
}
