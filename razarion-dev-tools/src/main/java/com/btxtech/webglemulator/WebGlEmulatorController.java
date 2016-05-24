package com.btxtech.webglemulator;

import com.btxtech.InstanceStringGenerator;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.units.ItemService;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.webglemulator.razarion.RazarionEmulator;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
    public Label aspectRatioLabel;
    public TextField xTranslationField;
    public TextField yTranslationField;
    public TextField zTranslationField;
    public Slider cameraZRotationSlider;
    public Slider cameraXRotationSlider;
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
    @Inject
    private ItemService itemService;
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
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
                webGlEmulator.drawArrays();
            }
        });
        // Only called if gets bigger
        centerPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
                webGlEmulator.drawArrays();
            }
        });

        ////////
//        camera.setTranslateX(970);
//        camera.setTranslateY(-80);
//        camera.setTranslateZ(0);
//        camera.setRotateX(Math.toRadians(90));
//        camera.setRotateZ(Math.toRadians(0));
//        projectionTransformation.setFovY(Math.toRadians(110));
        ////////

        fovSlider.valueProperty().set(Math.toDegrees(projectionTransformation.getFovY()));
        fovSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                projectionTransformation.setFovY(Math.toRadians(fovSlider.getValue()));
                webGlEmulator.drawArrays();
            }
        });
        cameraXRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateX()));
        cameraXRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                camera.setRotateX(Math.toRadians(cameraXRotationSlider.getValue()));
                webGlEmulator.drawArrays();
            }
        });
        cameraZRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateZ()));
        cameraZRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                camera.setRotateZ(Math.toRadians(cameraZRotationSlider.getValue()));
                webGlEmulator.drawArrays();
            }
        });
        xTranslationField.setText(Double.toString(camera.getTranslateX()));
        yTranslationField.setText(Double.toString(camera.getTranslateY()));
        zTranslationField.setText(Double.toString(camera.getTranslateZ()));

        webGlEmulator.init(canvas);
    }

    public void onMouseDragged(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition canvasPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());

        if (lastCanvasPosition != null) {
            DecimalPosition terrainOld = getTerrainPosition(lastCanvasPosition).toXY();
            DecimalPosition terrainNew = getTerrainPosition(canvasPosition).toXY();
            DecimalPosition deltaTerrain = terrainNew.sub(terrainOld);
            camera.setTranslateX(camera.getTranslateX() - deltaTerrain.getX());
            camera.setTranslateY(camera.getTranslateY() - deltaTerrain.getY());
            xTranslationField.setText(Double.toString(camera.getTranslateX()));
            yTranslationField.setText(Double.toString(camera.getTranslateY()));
            webGlEmulator.drawArrays();
        }
        lastCanvasPosition = canvasPosition;
    }

    public void onMouseMove(Event event) {

    }

    public void onMousePressed(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition canvasPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        DecimalPosition clipXY = webGlEmulator.toClipCoordinates(canvasPosition);
        Ray3d pickRay = projectionTransformation.createPickRay(clipXY);
        Ray3d worldPickRay = camera.toWorld(pickRay);
        Vertex groundMeshPosition = terrainSurface.calculatePositionGroundMesh(worldPickRay);
        System.out.println("Ground Mesh position: " + groundMeshPosition);
    }

    private Vertex getTerrainPosition(DecimalPosition canvasPosition) {
        DecimalPosition clipXY = webGlEmulator.toClipCoordinates(canvasPosition);
        Ray3d pickRay = projectionTransformation.createPickRay(clipXY);
        Ray3d worldPickRay = camera.toWorld(pickRay);
        return terrainSurface.calculatePositionOnZeroLevel(worldPickRay);
    }

    public void onMouseReleased(Event event) {
        lastCanvasPosition = null;
    }

    public void onDumpProjectionTransformatioClicked(ActionEvent actionEvent) {
        System.out.println("-----------Projection Transformation-----------------");
        System.out.println(projectionTransformation);
        System.out.println("-----------------------------------------------------");
    }

    public void onDumpCameraClicked(ActionEvent actionEvent) {
        System.out.println("---------------------Camera--------------------------");
        System.out.println(camera);
        System.out.println("-----------------------------------------------------");
    }

    public void onTestCaseButtonClicked(ActionEvent actionEvent) {
        System.out.println("---------------------Test Case-----------------------");
        System.out.println(camera);
        System.out.println(projectionTransformation);
        System.out.println(InstanceStringGenerator.generate(projectionTransformation.createMatrix()));
        System.out.println("-----------------------------------------------------");
    }

    public void xTranslationFieldChanged(ActionEvent actionEvent) {
        camera.setTranslateX(Double.parseDouble(xTranslationField.getText()));
        webGlEmulator.drawArrays();
    }

    public void yTranslationFieldChanged(ActionEvent actionEvent) {
        camera.setTranslateY(Double.parseDouble(yTranslationField.getText()));
        webGlEmulator.drawArrays();
    }

    public void zTranslationFieldChanged(ActionEvent actionEvent) {
        camera.setTranslateZ(Double.parseDouble(zTranslationField.getText()));
        webGlEmulator.drawArrays();
    }

    public void onTickButtonClicked(ActionEvent actionEvent) {
        itemService.tick();
        webGlEmulator.drawArrays();
    }

    public void onRestartButtonClicked(ActionEvent actionEvent) {
        itemService.setupItems();
        webGlEmulator.drawArrays();
    }
}
