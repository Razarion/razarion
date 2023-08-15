package com.btxtech.client;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.QuestProvider;
import com.btxtech.uiservice.ServerQuestProvider;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ClientServerQuestProvider implements ServerQuestProvider {
    @Inject
    private Caller<QuestProvider> questProvider;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @Override
    public void activateNextPossibleQuest() {
        questProvider.call(response -> {}, exceptionHandler.restErrorHandler("Calling QuestProvider.readMyOpenQuests()")).activateNextPossibleQuest();
    }
}
