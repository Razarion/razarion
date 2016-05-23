package com.btxtech.renderer;

import com.btxtech.renderer.emulation.webgl.WebGlEmulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.FlowPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 22.05.2016.
 */
public class WebGlEmulatorController implements Initializable {
    public FlowPane centerPanel;
    @Inject
    private WebGlEmulator webGlEmulator;
    public Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Only called if gets bigger
        centerPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
            }
        });
        // Only called if gets bigger
        centerPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
            }
        });

        webGlEmulator.init(canvas);
    }

    public void onMouseDragged(Event event) {

    }

    public void onMouseMove(Event event) {

    }

    public void onMousePressed(Event event) {

    }

    public void onMouseReleased(Event event) {

    }
}
