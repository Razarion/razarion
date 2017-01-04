package com.btxtech.webglemulator;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.12.2016.
 */
public class WebGlEmulatorSceneRenderer extends Abstract2dRenderer {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ShadowUiService shadowUiService;

    public void render() {
        preRender();

        try {
            ExtendedGraphicsContext egc = createExtendedGraphicsContext();

            drawTerrain(egc);

            // Camera view filed
            ViewField cameraView = projectionTransformation.calculateViewField(0);
            if (!cameraView.hasNullPosition()) {
                egc.strokeCurveDecimalPosition(cameraView.toList(), 0.5, Color.BLACK, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRender();
    }

    private void drawTerrain(ExtendedGraphicsContext egc) {
        egc.strokeVertexList(terrainUiService.getGroundVertexList().getVertices(), 0.2, Color.BLUE);

        for (Slope slope : terrainUiService.getSlopes()) {
            egc.strokeVertexList(slope.getMesh().getVertices(), 0.2, Color.RED);
        }

        egc.getGc().setFill(Color.GREEN);
        for (Map.Entry<TerrainObjectConfig, Collection<TerrainObjectPosition>> entry : terrainUiService.getTerrainObjectPositions().getMap().entrySet()) {
            TerrainObjectConfig terrainObjectConfig = entry.getKey();
            for (TerrainObjectPosition terrainObjectPosition : entry.getValue()) {
                egc.getGc().fillOval(terrainObjectPosition.getPosition().getX() - terrainObjectConfig.getRadius(),
                        terrainObjectPosition.getPosition().getY() - terrainObjectConfig.getRadius(),
                        2 * terrainObjectConfig.getRadius(), 2 * terrainObjectConfig.getRadius());
            }

        }

    }
}
