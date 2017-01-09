package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
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
    public void init(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes) {
        for (Map.Entry<BaseItemType, Collection<SyncBaseItemSimpleDto>> entry : itemTypes.entrySet()) {
            getChildren().add(new Label(entry.getKey().getName() + ": " + entry.getValue().size()));
        }
    }
}
