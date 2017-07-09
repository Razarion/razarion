package com.btxtech.webglemulator;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.webglemulator.razarion.WorkerEmulator;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
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
 * 31.05.2016.
 */
@Singleton
public class WorkerViewController implements Initializable {
    public static final String TILE_DISPLAY = "Tiles";
    public static final String ACCESS_DISPLAY = "Access";
    public static final String BOTH_DISPLAY = "Both";
    @FXML
    private ChoiceBox<String> displayChoiceBox;
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane anchorPanel;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @FXML
    private TextField mouseLabel;


    @Inject
    private WorkerEmulator workerEmulator;
    private WorkerViewRenderer workerViewRenderer;
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
        workerViewRenderer = workerEmulator.getWeldContainer().instance().select(WorkerViewRenderer.class).get();
        workerViewRenderer.init(canvas, zoom);
        scaleField.setText(String.format("%.2f", workerViewRenderer.getScale()));
        zoomSlider.setValue(workerViewRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
        displayChoiceBox.getItems().add(TILE_DISPLAY);
        displayChoiceBox.getItems().add(ACCESS_DISPLAY);
        displayChoiceBox.getItems().add(BOTH_DISPLAY);
        displayChoiceBox.getSelectionModel().select(TILE_DISPLAY);
        workerViewRenderer.setDisplay(TILE_DISPLAY);
        displayChoiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            workerViewRenderer.setDisplay(newValue);
            workerViewRenderer.render();
        });
    }

    public void update() {
        if (workerViewRenderer == null) {
            return;
        }
        workerViewRenderer.render();
    }

    public void onMouseDragged(Event event) {
        workerViewRenderer.shifting(event);
        workerViewRenderer.render();
    }

    public void onMouseReleased() {
        workerViewRenderer.stopShift();
        workerViewRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoom++;
        } else {
            zoom--;
        }
        workerViewRenderer.setZoom(zoom);
        workerViewRenderer.render();
    }

    boolean isActive() {
        return workerViewRenderer != null;
    }

    void close() {
        workerViewRenderer = null;
    }


    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        workerViewRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", workerViewRenderer.getScale()));
        workerViewRenderer.render();
    }

    public void onMousePressed(MouseEvent mouseEvent) {

    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        DecimalPosition position = workerViewRenderer.convertMouseToModel(mouseEvent);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
    }
}
