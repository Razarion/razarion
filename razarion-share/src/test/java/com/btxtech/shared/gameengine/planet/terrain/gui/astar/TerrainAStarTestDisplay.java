package com.btxtech.shared.gameengine.planet.terrain.gui.astar;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.pathing.AStar;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
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
public class TerrainAStarTestDisplay extends Application {
    private static DisplayDTO displayDTO;

    public static void show(TerrainShape terrainShape, SimplePath simplePath, AStar aStar) {
        displayDTO = new DisplayDTO();
        displayDTO.setTerrainShape(terrainShape);
        displayDTO.setSimplePath(simplePath);
        displayDTO.setaStar(aStar);
        Application.launch(TerrainAStarTestDisplay.class);
    }

    public static void show(TerrainShape terrainShape, PathingNodeWrapper pathingNodeWrapper) {
        displayDTO = new DisplayDTO();
        displayDTO.setTerrainShape(terrainShape);
        displayDTO.setPathingNodeWrapper(pathingNodeWrapper);
        Application.launch(TerrainAStarTestDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerrainTestApplication.fxml"));
        loader.setControllerFactory(param -> new TerrainAStarTestController(displayDTO));
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
