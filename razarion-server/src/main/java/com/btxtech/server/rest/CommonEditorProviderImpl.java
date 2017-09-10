package com.btxtech.server.rest;

import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.server.persistence.CommonEditorPersistence;
import com.btxtech.shared.datatypes.I18nStringEditor;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.rest.CommonEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 10.09.2017.
 */
public class CommonEditorProviderImpl implements CommonEditorProvider {
    @Inject
    private CommonEditorPersistence commonEditorPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;

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
}
