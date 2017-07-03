package com.btxtech.shared.gameengine.planet.terrain.gui.terrainshape;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class TerrainShapeTestDisplay extends Application {
    private static TerrainShape actual;

    public static void show(TerrainShape actual) {
        TerrainShapeTestDisplay.actual = actual;
        Application.launch(TerrainShapeTestDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerrainTestApplication.fxml"));
        loader.setControllerFactory(param -> new TerrainShapeTestController(actual));
        Parent root = loader.load();
        stage.setTitle("Terrain Shape Gui");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
