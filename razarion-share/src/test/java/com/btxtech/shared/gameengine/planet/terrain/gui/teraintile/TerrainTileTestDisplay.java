package com.btxtech.shared.gameengine.planet.terrain.gui.teraintile;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class TerrainTileTestDisplay extends Application {
    private static Collection<TerrainTile> expected;
    private static Collection<TerrainTile> actual;

    public static void show(Collection<TerrainTile> expected, Collection<TerrainTile> actual) {
        TerrainTileTestDisplay.expected = expected;
        TerrainTileTestDisplay.actual = actual;
        Application.launch(TerrainTileTestDisplay.class);
    }

    public static void show(Collection<TerrainTile> actual) {
        show(null, actual);
    }

    public static void show(TerrainTile actual) {
        show(null, Collections.singletonList(actual));
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerrainTestApplication.fxml"));
        loader.setControllerFactory(param -> new TerrainTileTestController(expected, actual));
        Parent root = loader.load();
        stage.setTitle("Terrain Tile Gui");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
