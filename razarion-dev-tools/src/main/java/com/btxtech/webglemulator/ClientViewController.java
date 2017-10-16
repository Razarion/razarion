package com.btxtech.webglemulator;

import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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
    private AnchorPane anchorPanel;
    @FXML
    private TextField mouseLabel;
    @FXML
    private Canvas canvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @Inject
    private Instance<ClientViewRenderer> instance;
    private ClientViewRenderer clientViewRenderer;
    private double zoom = 1.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            update();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            update();
        });
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
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
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    boolean isActive() {
        return clientViewRenderer != null;
    }

    void close() {
        clientViewRenderer = null;
    }

    public void onZoomResetButton(ActionEvent actionEvent) {
        setZoom(1);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        DecimalPosition position = clientViewRenderer.convertMouseToModel(mouseEvent);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
    }

    private void setZoom(double zoom) {
        clientViewRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", clientViewRenderer.getScale()));
        clientViewRenderer.render();
    }
}
