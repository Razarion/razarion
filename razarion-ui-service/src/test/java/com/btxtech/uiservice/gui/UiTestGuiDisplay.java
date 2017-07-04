package com.btxtech.uiservice.gui;

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
public class UiTestGuiDisplay extends Application {
    private static AbstractUiTestGuiRenderer abstractUiTestGuiRenderer;

    public static void show(AbstractUiTestGuiRenderer abstractUiTestGuiRenderer) {
        UiTestGuiDisplay.abstractUiTestGuiRenderer = abstractUiTestGuiRenderer;
        Application.launch(UiTestGuiDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UiTestGuiApplication.fxml"));
        loader.setControllerFactory(param -> new UiTestGuiControl(abstractUiTestGuiRenderer));
        Parent root = loader.load();
        stage.setTitle("Ui Test GUI");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
