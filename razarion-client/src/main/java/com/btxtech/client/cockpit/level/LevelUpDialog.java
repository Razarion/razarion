package com.btxtech.client.cockpit.level;

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
@Templated("LevelUpDialog.html#level-up-dialog")
public class LevelUpDialog extends Composite implements ModalDialogContent<Void> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label unlocked;


    @Override
    public void init(Void ignore) {
        unlocked.setText("Unlocked text");
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {

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
