package com.btxtech.gameengine;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GameEngineMonitor {
    @Inject
    private GameEngineMonitorController gameEngineMonitorController;

    public void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameengine/GameEngineMonitor.fxml"));
        loader.setControllerFactory(param -> gameEngineMonitorController);
        Parent root = loader.load();
        primaryStage.setTitle("GameEngineWorker Monitor");
        primaryStage.setScene(new Scene(root));
//        primaryStage.setX(-1279);
//        primaryStage.setY(182);
        primaryStage.setWidth(1277);
        primaryStage.setHeight(1016);
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + primaryStage.getX() + ":" + primaryStage.getY() + " " + primaryStage.getWidth() + ":" + primaryStage.getHeight()));
    }
}



