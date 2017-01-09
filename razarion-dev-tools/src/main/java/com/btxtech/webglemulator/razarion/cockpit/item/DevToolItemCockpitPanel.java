package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import com.btxtech.uiservice.cockpit.item.ItemCockpitPanel;
import com.btxtech.webglemulator.WebGlEmulatorController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
public class DevToolItemCockpitPanel implements ItemCockpitPanel {
    @Inject
    private WebGlEmulatorController webGlEmulatorController;

    @PostConstruct
    public void postConstruct() {
        webGlEmulatorController.getItemCockpitPanel().setVisible(false);
        webGlEmulatorController.getItemCockpitPanel().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @Override
    public void cleanPanels() {
        Platform.runLater(() -> {
            webGlEmulatorController.getItemCockpitPanel().getChildren().clear();
        });
    }

    @Override
    public void setInfoPanel(Object infoPanel) {
        Platform.runLater(() -> {
            webGlEmulatorController.getItemCockpitPanel().getChildren().add((Node) infoPanel);
        });
    }

    @Override
    public void setBuildupItemPanel(BuildupItemPanel buildupItemPanel) {
        DevToolBuildupItemPanel devToolBuildupItemPanel = (DevToolBuildupItemPanel) buildupItemPanel;
        Platform.runLater(() -> {
            webGlEmulatorController.getItemCockpitPanel().getChildren().add(devToolBuildupItemPanel.getHBox());
        });
    }

    @Override
    public void maximizeMinButton() {

    }

    @Override
    public void showPanel(boolean visible) {
        Platform.runLater(() -> {
            webGlEmulatorController.getItemCockpitPanel().setVisible(visible);
        });
    }
}
