package com.btxtech.webglemulator;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.webglemulator.razarion.RazarionEmulator;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 22.05.2016.
 */
public class WebGlEmulatorController implements Initializable {
    public AnchorPane centerPanel;
    public Slider fovSlider;
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private RazarionEmulator razarionEmulator;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;
    public Canvas canvas;
    private DecimalPosition lastCanvasPosition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Only called if gets bigger
        centerPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
                webGlEmulator.drawArrays();
            }
        });
        // Only called if gets bigger
        centerPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
                webGlEmulator.drawArrays();
            }
        });
        fovSlider.valueProperty().set(Math.toDegrees(projectionTransformation.getFovY()));
        fovSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                projectionTransformation.setFovY(Math.toRadians(fovSlider.getValue()));
                webGlEmulator.drawArrays();
            }
        });

        webGlEmulator.init(canvas);
    }

    public void onMouseDragged(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition canvasPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());

        // DecimalPosition terrainPosition = getTerrainPosition(event).toXY();
        // System.out.println("terrainPosition: " + terrainPosition);

        if (lastCanvasPosition != null) {
            DecimalPosition deltaCanvas = canvasPosition.sub(lastCanvasPosition);
            DecimalPosition terrainOld = getTerrainPosition(lastCanvasPosition).toXY();
            DecimalPosition terrainNew = getTerrainPosition(canvasPosition).toXY();
            DecimalPosition deltaTerrain = terrainNew.sub(terrainOld);
            camera.setTranslateX(camera.getTranslateX() - deltaTerrain.getX());
            camera.setTranslateY(camera.getTranslateY() - deltaTerrain.getY());
            webGlEmulator.drawArrays();
        }
        lastCanvasPosition = canvasPosition;
    }

    public void onMouseMove(Event event) {

    }

    public void onMousePressed(Event event) {
    }

    private Vertex getTerrainPosition(DecimalPosition canvasPosition) {
        DecimalPosition clipXY = webGlEmulator.toClipCoordinates(canvasPosition);
        Ray3d pickRay = projectionTransformation.createPickRay(clipXY);
        Ray3d worldPickRay = camera.toWorld(pickRay);
        return terrainSurface.calculatePositionOnTerrain(worldPickRay);
    }

    public void onMouseReleased(Event event) {
        lastCanvasPosition = null;
    }
}
