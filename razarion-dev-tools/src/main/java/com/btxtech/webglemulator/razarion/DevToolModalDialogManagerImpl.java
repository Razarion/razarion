package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.uiservice.dialog.ApplyListener;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Singleton
public class DevToolModalDialogManagerImpl extends AbstractModalDialogManager {
    @Override
    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, ApplyListener<QuestDescriptionConfig> applyListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quest bestanden");
            alert.setHeaderText(questDescriptionConfig.getPassedMessage());
            alert.setContentText("Reward: " + questDescriptionConfig.getXp() + "XP");
            alert.showAndWait();
            applyListener.onApply(null);
        });
    }

    @Override
    public void showLevelUp(UserContext userContext, ApplyListener<Void> applyListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Up");
            alert.setHeaderText(null);
            alert.setContentText("Neuer level");
            alert.showAndWait();
            applyListener.onApply(null);
        });
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Box picked");
            alert.setHeaderText(null);
            alert.setContentText("Box picked");
            alert.showAndWait();
        });
    }
}
