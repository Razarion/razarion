package com.btxtech.shared.gameengine.planet.terrain.gui.weld;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.pathing.AStar;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
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
    @Inject
    private WeldTestController gameEngineMonitorController;
    private static WeldDisplay uglyFxHackThis;

    public void show(Object[] userObject) {
        uglyFxHackThis = this;
        gameEngineMonitorController.setUserObjects(userObject);
        Application.launch(WeldDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerrainTestApplication.fxml"));
        loader.setControllerFactory(param -> uglyFxHackThis.gameEngineMonitorController);
        Parent root = loader.load();
        stage.setTitle("AStar Gui");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
