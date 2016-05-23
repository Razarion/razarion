package com.btxtech.gameengine.pathing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PathingMonitor extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/PathingMonitor.fxml"));
        primaryStage.setTitle("Razarion Pathing Monitor");
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setX(-867);
        primaryStage.setY(387);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



