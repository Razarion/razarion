package com.btxtech.shared.gui;

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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TestGuiControl implements Initializable {
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
    private AbstractTestGuiRenderer abstractTestGuiRenderer;
    private DecimalPosition mousePosition;

    public TestGuiControl(AbstractTestGuiRenderer abstractTestGuiRenderer) {
        this.abstractTestGuiRenderer = abstractTestGuiRenderer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        abstractTestGuiRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            abstractTestGuiRenderer.render();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            abstractTestGuiRenderer.render();
        });
        scaleField.setText(String.format("%.2f", abstractTestGuiRenderer.getScale()));
        zoomSlider.setValue(abstractTestGuiRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
    }


    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        abstractTestGuiRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", abstractTestGuiRenderer.getScale()));
        abstractTestGuiRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (abstractTestGuiRenderer.shifting(event)) {
            abstractTestGuiRenderer.render();
        }
    }

    public void onMouseReleased() {
        abstractTestGuiRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        mousePosition = abstractTestGuiRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", mousePosition.getX(), mousePosition.getY()));
        if(abstractTestGuiRenderer.onMouseMoved(mousePosition)) {
            abstractTestGuiRenderer.render();
        }
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = abstractTestGuiRenderer.convertMouseToModel(event);
        abstractTestGuiRenderer.onMousePressedTerrain(position);
    }

    public void onGenTestButtonClicked() {
        abstractTestGuiRenderer.onGenTestButtonClicked(mousePosition);
    }
}
