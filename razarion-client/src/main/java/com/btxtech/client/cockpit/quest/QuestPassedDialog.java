package com.btxtech.client.cockpit.quest;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
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
public class QuestPassedDialog extends Composite implements ModalDialogContent<QuestConfig> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label questPassedLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label rewardLabel;


    @Override
    public void init(QuestConfig questConfig) {
        questPassedLabel.setText(questConfig.getPassedMessage());
        rewardLabel.setText("Belohnung: " + questConfig.getXp() + "XP");
    }

    @Override
    public void customize(ModalDialogPanel<QuestConfig> modalDialogPanel) {

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
