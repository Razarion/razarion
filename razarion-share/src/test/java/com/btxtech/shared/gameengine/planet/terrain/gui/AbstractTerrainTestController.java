package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
public abstract class AbstractTerrainTestController implements Initializable {
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
    private AbstractTerrainTestRenderer abstractTerrainTestRenderer;

    protected abstract AbstractTerrainTestRenderer setupRenderer();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        abstractTerrainTestRenderer = setupRenderer();
        abstractTerrainTestRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            abstractTerrainTestRenderer.render();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            abstractTerrainTestRenderer.render();
        });
        scaleField.setText(String.format("%.2f", abstractTerrainTestRenderer.getScale()));
        zoomSlider.setValue(abstractTerrainTestRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
    }


    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        abstractTerrainTestRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", abstractTerrainTestRenderer.getScale()));
        abstractTerrainTestRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (abstractTerrainTestRenderer.shifting(event)) {
            abstractTerrainTestRenderer.render();
        }
    }

    public void onMouseReleased() {
        abstractTerrainTestRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = abstractTerrainTestRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = abstractTerrainTestRenderer.convertMouseToModel(event);
        onMousePressedTerrain(position);
    }

    // Override in subclass
    protected void onMousePressedTerrain(DecimalPosition position) {
        System.out.println("onMousePressedTerrain: " + position);
    }

}
