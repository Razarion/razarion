package com.btxtech.webglemulator;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 31.05.2016.
 */
@Singleton
public class WebGlEmulatorSceneController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane mainPanel;
    @Inject
    private Instance<WebGlEmulatorSceneRenderer> instance;
    private WebGlEmulatorSceneRenderer webGlEmulatorSceneRenderer;
    private double zoom = 1.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            update();
        });
        mainPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            update();
        });
        webGlEmulatorSceneRenderer = instance.get();
        webGlEmulatorSceneRenderer.init(canvas, zoom);
    }

    public void update() {
        if (webGlEmulatorSceneRenderer == null) {
            return;
        }
        webGlEmulatorSceneRenderer.render();
    }

    public void onMouseDragged(Event event) {
        webGlEmulatorSceneRenderer.shifting(event);
        webGlEmulatorSceneRenderer.render();
    }

    public void onMouseReleased() {
        webGlEmulatorSceneRenderer.stopShift();
        webGlEmulatorSceneRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoom++;
        } else {
            zoom--;
        }
        webGlEmulatorSceneRenderer.setZoom(zoom);
        webGlEmulatorSceneRenderer.render();
    }

    boolean isActive() {
        return webGlEmulatorSceneRenderer != null;
    }

    void close() {
        webGlEmulatorSceneRenderer = null;
    }
}
