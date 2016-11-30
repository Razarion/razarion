package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.cockpit.item.OwnMultiDifferentItemPanel;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 30.11.2016.
 */
public class DevToolOwnMultiDifferentItemPanel extends VBox implements OwnMultiDifferentItemPanel {
    @Override
    public void init(Map<BaseItemType, Collection<SyncBaseItem>> itemTypes) {
        for (Map.Entry<BaseItemType, Collection<SyncBaseItem>> entry : itemTypes.entrySet()) {
            getChildren().add(new Label(entry.getKey().getName() + ": " + entry.getValue().size()));
        }
    }
}
