package com.btxtech.client.cockpit.quest;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.uiservice.i18n.I18nHelper;
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
    @Inject
    @DataField
    private Label rewardXpLabel;
    @Inject
    @DataField
    private Label rewardRazarionLabel;
    @Inject
    @DataField
    private Label rewardCrystalLabel;

    @Override
    public void init(QuestDescriptionConfig questDescriptionConfig) {
        if (questDescriptionConfig.getPassedMessage() != null && !questDescriptionConfig.getPassedMessage().trim().isEmpty()) {
            questPassedLabel.setText(questDescriptionConfig.getPassedMessage());
        } else {
            questPassedLabel.setText(I18nHelper.getConstants().youPassedQuest(questDescriptionConfig.getTitle()));
        }
        rewardLabel.setText(I18nHelper.getConstants().reward());
        DisplayUtils.divDisplayState(rewardXpLabel, questDescriptionConfig.getXp() > 0);
        rewardXpLabel.setText(I18nHelper.getConstants().xpReward(questDescriptionConfig.getXp()));
        DisplayUtils.divDisplayState(rewardRazarionLabel, questDescriptionConfig.getRazarion() > 0);
        rewardRazarionLabel.setText(I18nHelper.getConstants().razarionReward(questDescriptionConfig.getXp()));
        DisplayUtils.divDisplayState(rewardCrystalLabel, questDescriptionConfig.getCrystal() > 0);
        rewardCrystalLabel.setText(I18nHelper.getConstants().crystalReward(questDescriptionConfig.getXp()));
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
