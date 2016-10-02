package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.uiservice.dialog.ApplyListener;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Singleton
public class DevToolModalDialogManagerImpl implements ModalDialogManager {
    @Override
    public void showQuestPassed(QuestConfig questConfig, ApplyListener<QuestConfig> applyListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quest bestanden");
            alert.setHeaderText(questConfig.getPassedMessage());
            alert.setContentText("Reward: " + questConfig.getXp() + "XP");
            alert.showAndWait();
            applyListener.onApply(null);
        });
    }

    @Override
    public void showLevelUp(UserContext userContext, ApplyListener<QuestConfig> applyListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Up");
            alert.setHeaderText(null);
            alert.setContentText("Neuer level");
            alert.showAndWait();
            applyListener.onApply(null);
        });
    }
}
