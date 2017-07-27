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
        System.out.println("++++ DevToolSideCockpit.show()");
    }

    @Override
    public void hide() {
        System.out.println("++++ DevToolSideCockpit.hide()");
    }

    @Override
    public void displayResources(int resources) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayResource(resources);
        });
    }

    @Override
    public void displayXps(int xp, int xp2LevelUp) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayXp(xp, xp2LevelUp);
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
    public void displayItemCount(int itemCount, int houseSpace) {
        System.out.println("++++ item count: " + itemCount + " / " + houseSpace);
    }
}
