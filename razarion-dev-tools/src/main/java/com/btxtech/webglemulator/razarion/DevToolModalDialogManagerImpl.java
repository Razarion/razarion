package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Singleton
public class DevToolModalDialogManagerImpl extends ModalDialogManager {
    @Override
    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quest bestanden");
            alert.setHeaderText(questDescriptionConfig.getPassedMessage());
            alert.setContentText("Reward: " + questDescriptionConfig.getXp() + "XP");
            alert.showAndWait();
            closeListener.run();
        });
    }

    @Override
    public void showLevelUp(LevelConfig newLevelConfig, Runnable closeListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Up");
            alert.setHeaderText(null);
            alert.setContentText("Neuer level");
            alert.showAndWait();
            closeListener.run();
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

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("showUseInventoryHouseSpaceExceeded");
            alert.setHeaderText(null);
            alert.setContentText("showUseInventoryHouseSpaceExceeded");
            alert.showAndWait();
        });
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("showUseInventoryHouseSpaceExceeded");
            alert.setHeaderText(null);
            alert.setContentText("showUseInventoryHouseSpaceExceeded");
            alert.showAndWait();
        });
    }

    @Override
    protected void showBaseLost(Runnable closeListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("showBaseLost");
            alert.setHeaderText(null);
            alert.setContentText("showBaseLost");
            alert.showAndWait();
            closeListener.run();
        });
    }

    @Override
    public void showLeaveStartTutorial(Runnable closeListener) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Tutorial Finished");
            alert.setHeaderText(null);
            alert.setContentText("Move to next planet");
            alert.showAndWait();
            closeListener.run();
        });
    }

    @Override
    public void showMessageImageDialog(String title, String message, Integer imageId) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Right Mouse Button");
            alert.setHeaderText(null);
            alert.setContentText("To execute the move command, use the rigt mouse button");
            alert.showAndWait();
        });
    }
}
