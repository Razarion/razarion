package com.btxtech.webglemulator.razarion.cockpit;

import com.btxtech.uiservice.cockpit.SideCockpit;
import com.btxtech.webglemulator.WebGlEmulatorController;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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
    public void displayResources(int resources) {
        Platform.runLater(() -> {
            webGlEmulatorController.displayResource(resources);
        });

    }
}
