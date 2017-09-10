package com.btxtech.client.editor.i18n;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.shared.datatypes.I18nStringEditor;
import com.btxtech.shared.rest.CommonEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Templated("I18nPanel.html#i18nPanel")
public class I18nPanel extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(I18nPanel.class.getName());
    @Inject
    private Caller<CommonEditorProvider> provider;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<I18nStringEditorModel, I18nStringEditorWidget> i18nTable;

    @PostConstruct
    public void init() {
        DOMUtil.removeAllElementChildren(i18nTable.getElement()); // Remove placeholder table row from template.
        provider.call((RemoteCallback<List<I18nStringEditor>>) i18nStringEditors -> i18nTable.setValue(i18nStringEditors.stream().map(i18nStringEditor -> {
            I18nStringEditorModel i18nStringEditorModel = new I18nStringEditorModel();
            i18nStringEditorModel.fromI18nStringEditor(i18nStringEditor);
            return i18nStringEditorModel;
        }).collect(Collectors.toList())), (message, throwable) -> {
            logger.log(Level.SEVERE, "CommonEditorProvider.loadAllI18NEntries failed: " + message, throwable);
            return false;
        }).loadAllI18NEntries();
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> provider.call((ignore) -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "CommonEditorProvider.saveI8NEntries failed: " + message, throwable);
            return false;
        }).saveI8NEntries(i18nTable.getValue().stream().filter(I18nStringEditorModel::checkDirty).map(I18nStringEditorModel::toI18nStringEditor).collect(Collectors.toList())));
        enableSaveButton(true);
    }
}
