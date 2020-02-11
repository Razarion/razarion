package com.btxtech.client.editor.i18n;

import com.btxtech.client.editor.sidebar.AbstractEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.I18nStringEditor;
import com.btxtech.shared.rest.CommonEditorProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Templated("I18nPanel.html#i18nPanel")
public class I18nPanel extends AbstractEditor {
    // private Logger logger = Logger.getLogger(I18nPanel.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<CommonEditorProvider> provider;
    @Inject
    @DataField
    private Span enStringMissingSpan;
    @Inject
    @DataField
    private Span deStringMissingSpan;
    @Inject
    @DataField
    private Button fixButton;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<I18nStringEditorModel, I18nStringEditorWidget> i18nTable;

    @PostConstruct
    public void init() {
        DOMUtil.removeAllElementChildren(i18nTable.getElement()); // Remove placeholder table row from template.
        provider.call((RemoteCallback<List<I18nStringEditor>>) i18nStringEditors -> {
            i18nTable.setValue(i18nStringEditors.stream().map(i18nStringEditor -> {
                I18nStringEditorModel i18nStringEditorModel = new I18nStringEditorModel();
                i18nStringEditorModel.fromI18nStringEditor(i18nStringEditor);
                return i18nStringEditorModel;
            }).collect(Collectors.toList()));
            setupMissingLabels();
        }, exceptionHandler.restErrorHandler("CommonEditorProvider.loadAllI18NEntries failed: ")).loadAllI18NEntries();

    }

    private void setupMissingLabels() {
        enStringMissingSpan.setTextContent(Long.toString(i18nTable.getValue().stream().filter(i18nStringEditorModel -> !i18nStringEditorModel.hasEnString()).count()));
        deStringMissingSpan.setTextContent(Long.toString(i18nTable.getValue().stream().filter(i18nStringEditorModel -> !i18nStringEditorModel.hasDeString()).count()));
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> provider.call((ignore) -> {
        }, exceptionHandler.restErrorHandler("CommonEditorProvider.saveI8NEntries failed: ")).saveI8NEntries(i18nTable.getValue().stream().filter(I18nStringEditorModel::checkDirty).map(I18nStringEditorModel::toI18nStringEditor).collect(Collectors.toList())));
        enableSaveButton(true);
    }

    @EventHandler("fixButton")
    private void fixButtonClick(ClickEvent event) {
        i18nTable.getValue().stream().filter(i18nStringEditorModel -> (i18nStringEditorModel.hasEnString() && !i18nStringEditorModel.hasDeString())
                || (!i18nStringEditorModel.hasEnString() && i18nStringEditorModel.hasDeString()) ).forEach(i18nStringEditorModel -> {
            if(!i18nStringEditorModel.hasEnString()) {
                i18nStringEditorModel.setEnString(getEnForDe(i18nStringEditorModel.getDeString()));
            }
            if(!i18nStringEditorModel.hasDeString()) {
                i18nStringEditorModel.setDeString(getDeForEn(i18nStringEditorModel.getEnString()));
            }
        });
        i18nTable.setValue(new ArrayList<>(i18nTable.getValue())); // Force GUI to update
        setupMissingLabels();
    }

    private String getEnForDe(String deString) {
        return i18nTable.getValue().stream().filter(I18nStringEditorModel::hasDeString).filter(I18nStringEditorModel::hasEnString).filter(i18nStringEditorModel -> i18nStringEditorModel.getDeString().equals(deString)).map(I18nStringEditorModel::getEnString).findFirst().orElse(null);
    }
    private String getDeForEn(String enString) {
        return i18nTable.getValue().stream().filter(I18nStringEditorModel::hasDeString).filter(I18nStringEditorModel::hasEnString).filter(i18nStringEditorModel -> i18nStringEditorModel.getEnString().equals(enString)).map(I18nStringEditorModel::getDeString).findFirst().orElse(null);
    }

}
