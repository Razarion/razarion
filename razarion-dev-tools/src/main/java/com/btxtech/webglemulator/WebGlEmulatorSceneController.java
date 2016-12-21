package com.btxtech.webglemulator;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 31.05.2016.
 */
@ApplicationScoped
public class WebGlEmulatorSceneController implements Initializable {
    private Canvas canvas;
    private AnchorPane mainPanel;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ShadowUiService shadowUiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            update();
        });
        mainPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            update();
        });
    }

    public void update() {
        if (canvas == null) {
            return;
        }
        System.out.println("zNear: " + projectionTransformation.getZNear());
        System.out.println("zFar: " + projectionTransformation.getZFar());

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

    private void drawTerrain(ExtendedGraphicsContext egc) {
        egc.strokeVertexList(terrainUiService.getGroundVertexList().getVertices(), 1, Color.BLUE);

        for (int slopeId : terrainUiService.getSlopeIds()) {
            egc.strokeVertexList(terrainUiService.getSlope(slopeId).getMesh().getVertices(), 1, Color.RED);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }
}
