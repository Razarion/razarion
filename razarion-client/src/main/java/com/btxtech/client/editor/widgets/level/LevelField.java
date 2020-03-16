package com.btxtech.client.editor.widgets.level;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 29.07.2017.
 */
@Templated("LevelField.html#field")
public class LevelField extends Composite {
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private LevelService levelService;
    @Inject
    @DataField
    private Button selectorButton;
    @Inject
    @DataField
    private Label nameLabel;
    private Integer levelId;
    private Consumer<Integer> levelIdConsumer;

    public void init(Integer levelId, Consumer<Integer> levelIdConsumer) {
        this.levelId = levelId;
        this.levelIdConsumer = levelIdConsumer;
        setupNameLabel();
    }

    @EventHandler("selectorButton")
    private void selectorButtonClicked(ClickEvent event) {
        modalDialogManager.show("Levels", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, LevelSelectionDialog.class, levelId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                levelId = selectedId;
                levelIdConsumer.accept(levelId);
                setupNameLabel();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel() {
        if (levelId != null) {
            LevelConfig levelConfig = levelService.getLevel(levelId);
            nameLabel.setText(levelConfig.getNumber() + " (" + levelConfig.getId() + ")");
        } else {
            nameLabel.setText("-");
        }
    }

}
