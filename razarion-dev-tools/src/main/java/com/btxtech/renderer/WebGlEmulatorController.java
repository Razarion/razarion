package com.btxtech.renderer;

import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.renderer.emulation.razarion.RazarionEmulator;
import com.btxtech.renderer.emulation.webgl.WebGlEmulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 22.05.2016.
 */
public class WebGlEmulatorController implements Initializable {
    public AnchorPane centerPanel;
    public Slider fovSlider;
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private RazarionEmulator razarionEmulator;
    @Inject
    private ProjectionTransformation projectionTransformation;
    public Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Only called if gets bigger
        centerPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
                webGlEmulator.drawArrays();
            }
        });
        // Only called if gets bigger
        centerPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
                webGlEmulator.drawArrays();
            }
        });
        fovSlider.valueProperty().set(Math.toDegrees(projectionTransformation.getFovY()));
        fovSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                projectionTransformation.setFovY(Math.toRadians(fovSlider.getValue()));
                webGlEmulator.drawArrays();
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
