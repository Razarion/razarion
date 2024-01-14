package com.btxtech.client.dialog.quest;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.common.DisplayUtils;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.QuestController;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Templated("QuestSelectionDialog.html#questSelectionTr")
@Deprecated
public class QuestConfigWidget implements TakesValue<QuestConfig>, IsElement {
    // private Logger logger = Logger.getLogger(QuestConfigWidget.class.getName());
    @Inject
    private Caller<QuestController> provider;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ClientModalDialogManagerImpl dialogManager;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    @AutoBound
    private DataBinder<QuestConfig> dataBinder;
    @Inject
    @DataField
    private TableRow questSelectionTr;
    @Inject
    @DataField
    private Button activateButton;
    @Inject
    @Bound
    @DataField
    private Label title;
    @Inject
    @Bound
    @DataField
    private Label description;
    @Inject
    @DataField
    private Label xp;
    @Inject
    @DataField
    private Label razarion;
    @Inject
    @DataField
    private Label crystal;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void setValue(QuestConfig questConfig) {
        DisplayUtils.divDisplayState(xp, questConfig.getXp() > 0);
        xp.setText(I18nHelper.getConstants().xpReward(questConfig.getXp()));
        DisplayUtils.divDisplayState(razarion, questConfig.getRazarion() > 0);
        razarion.setText(I18nHelper.getConstants().razarionReward(questConfig.getXp()));
        DisplayUtils.divDisplayState(crystal, questConfig.getCrystal() > 0);
        crystal.setText(I18nHelper.getConstants().crystalReward(questConfig.getXp()));
        dataBinder.setModel(questConfig);
    }

    @Override
    public QuestConfig getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return questSelectionTr;
    }

    @EventHandler("activateButton")
    private void onActivateButtonClicked(ClickEvent event) {
        if (gameUiControl.hasActiveServerQuest()) {
            dialogManager.showQuestionDialog(I18nHelper.getConstants().activate(), I18nHelper.getConstants().activeQuestAbort(), this::activateQuest, this::closeDialog);
        } else {
            activateQuest();
        }
    }

    private void activateQuest() {
        provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling QuestProvider.readMyOpenQuests()")).activateQuest(dataBinder.getModel().getId());
        closeDialog();
    }

    private void closeDialog() {
        dialogManager.close(modalDialogPanel);
    }

    public void setModalDialogPanel(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

}
