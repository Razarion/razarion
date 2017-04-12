package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
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
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TerrainTestController implements Initializable {
    private final Collection<TerrainTile> expected;
    private final Collection<TerrainTile> actual;
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
    private TerrainTestRenderer terrainTestRenderer;
    private TriangleContainer triangleContainer;

    public TerrainTestController(Collection<TerrainTile> expected, Collection<TerrainTile> actual) {
        this.expected = expected;
        this.actual = actual;
        triangleContainer = new TriangleContainer(expected, actual);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        terrainTestRenderer = new TerrainTestRenderer(expected, actual, triangleContainer);
        terrainTestRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            terrainTestRenderer.render();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            terrainTestRenderer.render();
        });
        scaleField.setText(String.format("%.2f", terrainTestRenderer.getScale()));
        zoomSlider.setValue(terrainTestRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        terrainTestRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", terrainTestRenderer.getScale()));
        terrainTestRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (terrainTestRenderer.shifting(event)) {
            terrainTestRenderer.render();
        }
    }

    public void onMouseReleased() {
        terrainTestRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = terrainTestRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = terrainTestRenderer.convertMouseToModel(event);
        triangleContainer.printTrianglesAt(position);
    }

}
