package com.btxtech.server.debuggui;

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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * on 07.08.2018.
 */
@Singleton
public class ServerGuiController implements Initializable {
    @Inject
    private ServerDebugRenderer serverDebugRenderer;
    @FXML
    private AnchorPane anchorPanel;
    @FXML
    private Canvas canvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @FXML
    private TextField mouseLabel;
    @FXML
    private TextField zMinField;
    @FXML
    private TextField zMaxField;
    private DecimalPosition mousePosition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverDebugRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            serverDebugRenderer.render();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            serverDebugRenderer.render();
        });
        scaleField.setText(String.format("%.2f", serverDebugRenderer.getScale()));
        zoomSlider.setValue(serverDebugRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        serverDebugRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", serverDebugRenderer.getScale()));
        serverDebugRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (serverDebugRenderer.shifting(event)) {
            serverDebugRenderer.render();
        }
    }

    public void onMouseReleased() {
        serverDebugRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        mousePosition = serverDebugRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", mousePosition.getX(), mousePosition.getY()));
//        if(serverDebugRenderer.onMouseMoved(mousePosition)) {
//            serverDebugRenderer.render();
//        }
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = serverDebugRenderer.convertMouseToModel(event);
        // serverDebugRenderer.onMousePressedTerrain(position);
    }
}
