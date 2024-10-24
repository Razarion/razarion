package com.btxtech.shared.gameengine.planet.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Singleton
public class WeldDisplay extends Application {

    private WeldTestController gameEngineMonitorController;
    private static WeldDisplay uglyFxHackThis;

    @Inject
    public WeldDisplay(WeldTestController gameEngineMonitorController) {
        this.gameEngineMonitorController = gameEngineMonitorController;
    }

    public void show(Object[] userObject) {
        uglyFxHackThis = this;
        gameEngineMonitorController.setUserObjects(userObject);
        Application.launch(WeldDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WeldDisplay.fxml"));
        loader.setControllerFactory(param -> uglyFxHackThis.gameEngineMonitorController);
        Parent root = loader.load();
        stage.setTitle("Test Weld Display");
        stage.setScene(new Scene(root));
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(1600);
        stage.setHeight(1400);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
