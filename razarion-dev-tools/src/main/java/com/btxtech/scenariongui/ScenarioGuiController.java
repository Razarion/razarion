package com.btxtech.scenariongui;

import com.btxtech.scenariongui.scenario.Scenario;
import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 25.06.2016.
 */
public class ScenarioGuiController implements Initializable {
    public AnchorPane anchorPanel;
    public Canvas canvas;
    public ComboBox<Scenario.ScenarioEntry> scenarioBox;
    public Slider zoomSlider;
    public TextField scaleField;
    public TextField mouseLabel;
    private ScenarioRenderer scenarioRenderer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                scenarioRenderer.render();
            }
        });
        anchorPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                scenarioRenderer.render();
            }
        });
        scenarioBox.getItems().addAll(Scenario.getScenarios());
        scenarioRenderer = new ScenarioRenderer(canvas, Scenario.getScenarios().get(0).getScenario(), 1);
        scaleField.setText("1");
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                setZoom(zoomSlider.getValue());
            }
        });
        scenarioBox.setValue(Scenario.getScenarios().get(0));
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        scenarioRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", scenarioRenderer.getScale()));
        scenarioRenderer.render();
    }


    public void onMouseDragged(Event event) {
        if (scenarioRenderer.shifting(event)) {
            scenarioRenderer.render();
        }
    }

    public void onMouseReleased(Event event) {
        scenarioRenderer.stopShift();
    }

    public void scenarioBoxChanged() {
        scenarioRenderer.setScenario(scenarioBox.getValue().getScenario());
        scenarioRenderer.render();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = scenarioRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        boolean redraw = scenarioRenderer.getScenario().onMouseMove(position);
        if (redraw) {
            scenarioRenderer.render();
        }
    }

    public void onMousePressed(Event event) {
        DecimalPosition position = scenarioRenderer.convertMouseToModel(event);
        System.out.println("Mouse pressed at: " + InstanceStringGenerator.generate(position));
        boolean redraw = scenarioRenderer.getScenario().onMouseDown(position);
        if (redraw) {
            scenarioRenderer.render();
        }
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onGenerateButtonPressed() {
        scenarioRenderer.getScenario().onGenerate();
    }

    public void onCmd1ButtonPressed() {
        scenarioRenderer.getScenario().onCmd1();
    }
}
