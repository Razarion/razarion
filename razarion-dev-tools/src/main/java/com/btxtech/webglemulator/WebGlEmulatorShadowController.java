package com.btxtech.webglemulator;

import com.btxtech.uiservice.renderer.CameraMovedEvent;
import com.btxtech.webglemulator.webgl.WebGlEmulatorShadow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 31.05.2016.
 */
@Singleton
public class WebGlEmulatorShadowController implements Initializable {
    @Inject
    private WebGlEmulatorShadow webGlEmulatorShadow;
    public AnchorPane mainPanel;
    public Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                webGlEmulatorShadow.drawArrays();
            }
        });
        mainPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                webGlEmulatorShadow.drawArrays();
            }
        });
    }

    public void onCameraMovedEvent(@Observes CameraMovedEvent cameraMovedEvent) {
        if (canvas == null) {
            return;
        }
        webGlEmulatorShadow.drawArrays();
    }


    public Canvas getCanvas() {
        return canvas;
    }
}
