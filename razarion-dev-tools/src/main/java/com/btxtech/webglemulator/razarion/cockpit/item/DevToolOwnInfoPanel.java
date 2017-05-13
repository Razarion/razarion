package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.cockpit.item.OwnInfoPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Created by Beat
 * 30.09.2016.
 */
public class DevToolOwnInfoPanel extends VBox implements OwnInfoPanel {
    @Override
    public void init(BaseItemType baseItemType, int count) {
        getChildren().add(new Label("OwnInfoPanel"));
        getChildren().add(new Label("Type: " + baseItemType.getName()));
        getChildren().add(new Label("Description: " + baseItemType.getI18nDescription()));
    }
}
