package com.btxtech.server.debuggui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Beat
 * on 07.08.2018.
 */
public class StaticApplication extends Application {
    private static ServerGuiController serverGuiController;

    // Needed due to JavaFX restrictions
    public static void doLaunch(ServerGuiController serverGuiController) {
        StaticApplication.serverGuiController = serverGuiController;
        launch(StaticApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServerGuiController.fxml"));
        loader.setControllerFactory(param -> serverGuiController);
        Parent root = loader.load();
        stage.setTitle("Server Gui");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(984);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
