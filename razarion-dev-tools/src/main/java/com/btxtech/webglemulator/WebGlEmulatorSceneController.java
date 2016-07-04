package com.btxtech.webglemulator;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.CameraMovedEvent;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 31.05.2016.
 */
@Singleton
public class WebGlEmulatorSceneController implements Initializable {
    public Canvas canvas;
    public AnchorPane mainPanel;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ShadowUiService shadowUiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                update();
            }
        });
        mainPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                update();
            }
        });
    }

    public void update() {
        if (canvas == null) {
            return;
        }
        System.out.println("zNear: " + projectionTransformation.calculateZNear());
        System.out.println("zFar: " + projectionTransformation.calculateZFar());

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();

        try {

            double scale = 0.1;
            DecimalPosition shift = new DecimalPosition(0, 0);

            gc.translate(canvasWidth / 2.0, canvasHeight / 2.0);
            gc.scale(scale, -scale);
            gc.translate(shift.getX(), shift.getY());

            ExtendedGraphicsContext egc = new ExtendedGraphicsContext(gc);

            drawTerrain(egc);

            // Camera view filed
            ViewField cameraView = projectionTransformation.calculateViewField(0);
            if (!cameraView.hasNullPosition()) {
                egc.strokeCurveDecimalPosition(cameraView.toList(), 10, Color.BLACK, false);
            }
//            ViewField cameraView1 = projectionTransformation.calculateViewField(100);
//            if (!cameraView1.hasNullPosition()) {
//                egc.strokeCurveDecimalPosition(cameraView1.toList(), 10, Color.RED, false);
//            }
//            ViewField cameraView2 = projectionTransformation.calculateViewField(-100);
//            if (!cameraView2.hasNullPosition()) {
//                egc.strokeCurveDecimalPosition(cameraView2.toList(), 10, Color.BLUE, false);
//            }

            // Shadow view field
            ViewField shadowSourceView = shadowUiService.calculateViewField();
            if (!shadowSourceView.hasNullPosition()) {
                egc.strokeCurveDecimalPosition(shadowSourceView.toList(), 10, Color.GREEN, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gc.restore();
    }

    public void onCameraMovedEvent(@Observes CameraMovedEvent cameraMovedEvent) {
        if (canvas == null) {
            return;
        }
        update();
    }

    private void drawTerrain(ExtendedGraphicsContext egc) {
        egc.strokeVertexList(terrainSurface.getGroundVertexList().getVertices(), 1, Color.BLUE);

        for (int slopeId : terrainSurface.getSlopeIds()) {
            egc.strokeVertexList(terrainSurface.getSlope(slopeId).getMesh().getVertices(), 1, Color.RED);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }
}
