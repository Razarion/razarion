package com.btxtech.client.cockpit.quest;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Templated("QuestPassedDialog.html#quest-passed-dialog")
public class QuestPassedDialog extends Composite implements ModalDialogContent<QuestDescriptionConfig> {
    @Inject
    @DataField
    private Label questPassedLabel;
    @Inject
    @DataField
    private Label rewardLabel;

    @Override
    public void init(QuestDescriptionConfig questDescriptionConfig) {
        if (questDescriptionConfig.getPassedMessage() != null) {
            questPassedLabel.setText(questDescriptionConfig.getPassedMessage());
        } else {
            questPassedLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
        rewardLabel.setText(I18nHelper.getConstants().reward() + ": " + I18nHelper.getConstants().xpRewards(questDescriptionConfig.getXp()));
    }

    @Override
    public void customize(ModalDialogPanel<QuestDescriptionConfig> modalDialogPanel) {

    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void onClose() {
        // Ignore
    }
}
