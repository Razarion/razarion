package com.btxtech.renderer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jboss.weld.environment.se.Weld;

/**
 * Created by Beat
 * 18.09.2015.
 */
public class FxMain extends Application {
    private Weld weld;

    @Override
    public void init() {
        weld = new Weld();
    }

    @Override
    public void start(Stage stage) throws Exception {
        weld.initialize().instance().select(FxCdiGui.class).get().start(stage);
    }

    @Override
    public void stop() {
        weld.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
