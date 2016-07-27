package com.btxtech.webglemulator;

import com.btxtech.webglemulator.razarion.RazarionEmulator;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

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
        // stage.setFullScreen(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/WebGlEmulator.fxml"));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return webGlEmulatorController;
            }
        });
        Parent root = (Parent) loader.load();
        stage.setTitle("WebGL FX Emulator");
        stage.setScene(new Scene(root));
        // stage.setX(-1279);
        // stage.setY(182);
        // stage.setWidth(1277);
        // stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight());
            }
        });
        razarionEmulator.run();
    }

}
