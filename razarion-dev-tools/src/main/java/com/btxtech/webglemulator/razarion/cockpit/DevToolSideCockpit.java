package com.btxtech.webglemulator.razarion.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.SideCockpit;
import com.btxtech.webglemulator.WebGlEmulatorController;
import javafx.application.Platform;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 16.11.2016.
 */
@ApplicationScoped
public class DevToolSideCockpit implements SideCockpit {
    @Inject
    private WebGlEmulatorController webGlEmulatorController;

    @Override
    public void show() {
        System.out.println("++++ DevToolSideCockpit.showStoryCover()");
    }

    @Override
    public void displayResources(int resources) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayResource(resources);
        });
    }

    @Override
    public void displayXps(int xp) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayXp(xp);
        });
    }

    @Override
    public void displayLevel(int levelNumber) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayLevel(levelNumber);
        });
    }

    @Override
    public Rectangle getInventoryDialogButtonLocation() {
        return new Rectangle(10, 10, 100, 20);
    }

    @Override
    public void displayItemCount(int itemCount) {
        System.out.println("++++ item count: " + itemCount);
    }
}
