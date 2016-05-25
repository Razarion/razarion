package com.btxtech.webglemulator;

import com.btxtech.webglemulator.razarion.RazarionEmulator;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 19.09.2015.
 */
@Singleton
public class FxCdiGui {
    @Inject
    private Instance<Object> instance;
    @Inject
    private RazarionEmulator razarionEmulator;

    public void start(final Stage stage) throws Exception {
        stage.setFullScreen(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WebGlEmulator.fxml"));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return instance.select(param).get();
            }
        });
        Parent root = (Parent) loader.load();
        stage.setTitle("WebGL FX Emulator");
        stage.setScene(new Scene(root));
        stage.setX(-1288);
        stage.setY(168);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY());
            }
        });
        razarionEmulator.process();
    }

}
