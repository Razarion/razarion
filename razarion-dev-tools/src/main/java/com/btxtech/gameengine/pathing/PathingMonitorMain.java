package com.btxtech.gameengine.pathing;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PathingMonitorMain extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/pathing/PathingMonitor.fxml"));
        primaryStage.setTitle("Razarion Pathing Monitor");
        primaryStage.setScene(new Scene(root));
        primaryStage.setX(-1279);
        primaryStage.setY(182);
        primaryStage.setWidth(1277);
        primaryStage.setHeight(1016);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing. Windows position: " + primaryStage.getX() + ":" + primaryStage.getY() + " " + primaryStage.getWidth() + ":" + primaryStage.getHeight());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}



