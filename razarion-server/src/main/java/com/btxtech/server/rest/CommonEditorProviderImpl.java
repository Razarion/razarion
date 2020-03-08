package com.btxtech.server.rest;

import com.btxtech.server.persistence.CommonEditorPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.shared.datatypes.I18nStringEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CommonEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 10.09.2017.
 */
public class CommonEditorProviderImpl implements CommonEditorProvider {
    @Inject
    private CommonEditorPersistence commonEditorPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    @Override
    public List<I18nStringEditor> loadAllI18NEntries() {
        try {
            return commonEditorPersistence.loadAllI18NEntries();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void saveI8NEntries(List<I18nStringEditor> i18nStringEditors) {
        try {
            commonEditorPersistence.saveI8NEntries(i18nStringEditors);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> getAllBotsFromPlanet(int planetId) {
        try {
            return serverGameEngineCrudPersistence.readBotConfigs().stream().map(botConfig -> new ObjectNameId(botConfig.getId(), botConfig.getInternalName())).collect(Collectors.toList());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public String getInternalNameBot(int botId) {
        try {
            return commonEditorPersistence.getInternalNameBot(botId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
