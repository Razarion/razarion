package com.btxtech.webglemulator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 31.05.2016.
 */
@Singleton
public class WebGlEmulatorShadowController implements Initializable {
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Canvas canvas;
    private boolean active;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
            }
        });
        mainPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
            }
        });
        active = true;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
