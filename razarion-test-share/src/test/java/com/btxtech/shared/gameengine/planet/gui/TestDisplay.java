package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.TestShareDagger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the visual test debugger (formerly WeldDisplay).
 * Renamed because Weld DI was replaced by Dagger long ago.
 */
public class TestDisplay extends Application {

    private static TestShareDagger testShareDagger;
    private static Object[] userObject;

    public static void show(Object[] userObject, TestShareDagger testShareDagger) {
        TestDisplay.testShareDagger = testShareDagger;
        TestDisplay.userObject = userObject;
        Application.launch(TestDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TestDisplay.fxml"));
        loader.setControllerFactory(param -> new TestController(TestDisplay.userObject, TestDisplay.testShareDagger));
        Parent root = loader.load();
        stage.setTitle("Razarion Test Display");
        stage.setScene(new Scene(root));
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(1600);
        stage.setHeight(1400);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage closing. Window: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
    }
}
