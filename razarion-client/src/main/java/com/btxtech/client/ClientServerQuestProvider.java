package com.btxtech.client;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.QuestController;
import com.btxtech.uiservice.ServerQuestProvider;

import javax.inject.Singleton;
import javax.inject.Inject;

@Singleton
public class ClientServerQuestProvider implements ServerQuestProvider {

    private Caller<QuestController> questProvider;

    private ClientExceptionHandlerImpl exceptionHandler;

    @Inject
    public ClientServerQuestProvider(ClientExceptionHandlerImpl exceptionHandler, Caller<com.btxtech.shared.rest.QuestController> questProvider) {
        this.exceptionHandler = exceptionHandler;
        this.questProvider = questProvider;
    }

    @Override
    public void activateNextPossibleQuest() {
        questProvider.call(response -> {}, exceptionHandler.restErrorHandler("Calling QuestProvider.readMyOpenQuests()")).activateNextPossibleQuest();
    }
}
