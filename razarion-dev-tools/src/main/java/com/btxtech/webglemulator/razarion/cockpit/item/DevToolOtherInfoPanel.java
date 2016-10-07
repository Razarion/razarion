package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.uiservice.cockpit.item.OtherInfoPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class DevToolOtherInfoPanel extends VBox implements OtherInfoPanel {
    @Override
    public void init(SyncItem targetSelection) {
        getChildren().add(new Label("OwnInfoPanel"));
        getChildren().add(new Label("Type: " + targetSelection.getItemType().getName()));
        getChildren().add(new Label("Description: " + targetSelection.getItemType().getDescription()));
        getChildren().add(new Label("Player: " + ((SyncBaseItem)targetSelection).getBase().getName()));
    }
}
