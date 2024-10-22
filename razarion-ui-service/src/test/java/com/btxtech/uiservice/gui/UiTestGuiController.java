package com.btxtech.uiservice.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.gui.userobject.MouseMoveRender;
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
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class UiTestGuiController implements Initializable {
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
    private final AbstractUiTestGuiRenderer abstractUiTestGuiRenderer;
    private MouseMoveRender mouseMoveCallback;

    public UiTestGuiController(AbstractUiTestGuiRenderer abstractUiTestGuiRenderer, Object[] customRenders) {
        this.abstractUiTestGuiRenderer = abstractUiTestGuiRenderer;
        Arrays.stream(customRenders).forEach(customRender -> {
            if (customRender instanceof MouseMoveRender) {
                mouseMoveCallback = (MouseMoveRender) customRender;
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        abstractUiTestGuiRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            abstractUiTestGuiRenderer.render();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            abstractUiTestGuiRenderer.render();
        });
        scaleField.setText(String.format("%.2f", abstractUiTestGuiRenderer.getScale()));
        zoomSlider.setValue(abstractUiTestGuiRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
    }


    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        abstractUiTestGuiRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", abstractUiTestGuiRenderer.getScale()));
        abstractUiTestGuiRenderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (abstractUiTestGuiRenderer.shifting(event)) {
            abstractUiTestGuiRenderer.render();
        }
    }

    public void onMouseReleased() {
        abstractUiTestGuiRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = abstractUiTestGuiRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        if (mouseMoveCallback != null) {
            mouseMoveCallback.onMouseMove(position);
            abstractUiTestGuiRenderer.setMouseMoveCallback(mouseMoveCallback);
            abstractUiTestGuiRenderer.render();
            abstractUiTestGuiRenderer.setMouseMoveCallback(null);
        }
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = abstractUiTestGuiRenderer.convertMouseToModel(event);
        onMousePressedTerrain(position);
    }

    // Override in subclass
    protected void onMousePressedTerrain(DecimalPosition position) {

    }

}
