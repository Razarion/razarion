package com.btxtech.client.dialog.quest;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestProvider;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 26.10.2016.
 */
@Templated("QuestSelectionDialog.html#quest-selection-dialog")
public class QuestSelectionDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(QuestSelectionDialog.class.getName());
    @Inject
    private Caller<QuestProvider> provider;
    @Inject
    @DataField
    private Label textLabel;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<QuestConfig, QuestConfigWidget> questTable;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        DOMUtil.removeAllElementChildren(questTable.getElement()); // Remove placeholder table row from template.
        questTable.addComponentCreationHandler(questConfigWidget -> questConfigWidget.setModalDialogPanel(modalDialogPanel));
        provider.call(response -> setupGui((List<QuestConfig>) response), (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling QuestProvider.readMyOpenQuests() failed: " + message, throwable);
            return false;
        }).readMyOpenQuests();
    }

    private void setupGui(List<QuestConfig> questConfigs) {
        questTable.setValue(questConfigs);
        if(questConfigs.isEmpty()) {
            textLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            textLabel.setText(I18nHelper.getConstants().noMoreQuests());
        } else {
            textLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onClose() {
        // Ignore
    }

}