package com.btxtech.webglemulator;

import com.btxtech.webglemulator.razarion.RazarionEmulator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.09.2015.
 */
@ApplicationScoped
public class FxCdiGui {
    @Inject
    private RazarionEmulator razarionEmulator;
    @Inject
    private WebGlEmulatorController webGlEmulatorController;

    public void start(final Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/WebGlEmulator.fxml"));
        loader.setControllerFactory(param -> webGlEmulatorController);
        Parent root = loader.load();
        stage.setTitle("WebGL FX Emulator");
        stage.setScene(new Scene(root));
//        stage.setX(-1279);
//        stage.setY(182);
//        stage.setWidth(1277);
//        stage.setHeight(984);
        stage.show();
        stage.setOnCloseRequest(we -> System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight()));
        razarionEmulator.run();
    }

}
