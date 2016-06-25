package com.btxtech.scenariongui;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.scenariongui.scenario.Scenario;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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

    public void onZoomResetButon() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        scenarioRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", scenarioRenderer.getScale()));
        scenarioRenderer.render();
    }


    public void onMouseDragged(Event event) {
        scenarioRenderer.shifting(event);
        scenarioRenderer.render();
    }

    public void onMouseReleased(Event event) {
        scenarioRenderer.stopShift();
    }

    public void scenarioBoxChanged() {
        scenarioRenderer.setScenario(scenarioBox.getValue().getScenario());
        scenarioRenderer.render();
    }

    public void onMouseMoved(Event event) {
        Index position = scenarioRenderer.convertMouseToModel(event).getPosition();
        mouseLabel.setText(String.format("%d:%d", position.getX(), position.getY()));
    }
}
