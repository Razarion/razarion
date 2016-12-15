package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.item.BuildupItem;
import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Created by Beat
 * 03.11.2016.
 */
public class DevToolBuildupItemPanel extends BuildupItemPanel {
    private HBox hBox = new HBox();

    @Override
    protected void clear() {
        hBox.getChildren().clear();
    }

    @Override
    protected void setBuildupItem(List<BuildupItem> buildupItems) {
        for (BuildupItem buildupItem : buildupItems) {
            Button button = new Button(buildupItem.getItemType().getName());
            button.setOnMouseClicked(event -> buildupItem.onBuild());
            hBox.getChildren().add(button);
        }
    }

    public HBox getHBox() {
        return hBox;
    }

    @Override
    protected Rectangle getBuildButtonLocation(BuildupItem buildupItem) {
        return new Rectangle(100, 300, 50, 50);
    }
}
