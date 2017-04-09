package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Collection;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class TerrainTestApplication extends Application {
    private static Collection<TerrainTile> expected;
    private static Collection<TerrainTile> actual;

    public static void show(Collection<TerrainTile> expected, Collection<TerrainTile> actual) {
        TerrainTestApplication.expected = expected;
        TerrainTestApplication.actual = actual;
        Application.launch(TerrainTestApplication.class);
    }

    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerrainTestApplication.fxml"));
        loader.setControllerFactory(param -> new TerrainTestController(expected, actual));
        Parent root = loader.load();
        stage.setTitle("Terrain Test Gui");
        stage.setScene(new Scene(root));
        stage.setX(-1279);
        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
