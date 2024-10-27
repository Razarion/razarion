package com.btxtech.shared.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class ShareTestGuiDisplay extends Application {
    private static AbstractTestGuiRenderer abstractTestGuiRenderer;

    public static void show(AbstractTestGuiRenderer abstractTestGuiRenderer) {
        ShareTestGuiDisplay.abstractTestGuiRenderer = abstractTestGuiRenderer;
        Application.launch(ShareTestGuiDisplay.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TestGuiApplication.fxml"));
        loader.setControllerFactory(param -> new TestGuiControl(abstractTestGuiRenderer));
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
