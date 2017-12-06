package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.uiservice.cockpit.item.ItemContainerPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

/**
 * Created by Beat
 * on 05.12.2017.
 */
public class DevToolItemContainerPanel extends ItemContainerPanel {
    private VBox vBox = new VBox();
    private Button button;
    private Label label;

    @PostConstruct
    public void postConstruct() {
        label = new Label();
        vBox.getChildren().add(label);
        button = new Button("Unload");
        button.setOnMouseClicked(event -> onUnloadPressed());
        vBox.getChildren().add(button);
    }

    public VBox getVBox() {
        return vBox;
    }

    @Override
    protected void updateGui(boolean enabled, int count) {
        if (enabled) {
            button.setDisable(false);
            label.setText("Loaded items: " + count);
        } else {
            button.setDisable(true);
            label.setText("Container is empty");
        }
    }
}
