package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.TestShareDagger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class WeldDisplay extends Application {

    private static TestShareDagger testShareDagger;
    private static Object[] userObject;

    public static void show(Object[] userObject, TestShareDagger testShareDagger) {
        WeldDisplay.testShareDagger = testShareDagger;
        WeldDisplay.userObject = userObject;
        Application.launch(WeldDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WeldDisplay.fxml"));
        loader.setControllerFactory(param -> new WeldTestController(WeldDisplay.userObject, WeldDisplay.testShareDagger));
        Parent root = loader.load();
        stage.setTitle("Test Weld Display");
        stage.setScene(new Scene(root));
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(1600);
        stage.setHeight(1400);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
