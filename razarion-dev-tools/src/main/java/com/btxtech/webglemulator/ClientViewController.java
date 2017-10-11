package com.btxtech.webglemulator;

import com.btxtech.uiservice.terrain.TerrainUiService;
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
public class ClientViewController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane mainPanel;
    @Inject
    private Instance<ClientViewRenderer> instance;
    private ClientViewRenderer clientViewRenderer;
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
        clientViewRenderer = instance.get();
        clientViewRenderer.init(canvas, zoom);
    }

    public void update() {
        if (clientViewRenderer == null) {
            return;
        }
        clientViewRenderer.render();
    }

    public void onMouseDragged(Event event) {
        clientViewRenderer.shifting(event);
        clientViewRenderer.render();
    }

    public void onMouseReleased() {
        clientViewRenderer.stopShift();
        clientViewRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoom++;
        } else {
            zoom--;
        }
        clientViewRenderer.setZoom(zoom);
        clientViewRenderer.render();
    }

    boolean isActive() {
        return clientViewRenderer != null;
    }

    void close() {
        clientViewRenderer = null;
    }
}
