package com.btxtech.webglemulator;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainConstants;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.Collection;
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
    @Inject
    private GameUiControl gameUiControl;

    public void render() {
        preRender();

        try {
            ExtendedGraphicsContext egc = createExtendedGraphicsContext();

            drawTerrain(egc);

            // Camera view filed
            ViewField cameraView = projectionTransformation.calculateViewField(0);
            if (!cameraView.hasNullPosition()) {
                egc.strokePolygon(cameraView.toList(), 2.0, Color.BLACK, false);
            }

            // Ground Mesh
            Rectangle groundRect = gameUiControl.getPlanetConfig().getGroundMeshDimension();
            Rectangle2D groundMesh = new Rectangle2D(groundRect.startX() * TerrainConstants.GROUND_NODE_EDGE_LENGTH,
                    groundRect.startY() * TerrainConstants.GROUND_NODE_EDGE_LENGTH,
                    groundRect.width() * TerrainConstants.GROUND_NODE_EDGE_LENGTH,
                    groundRect.height() * TerrainConstants.GROUND_NODE_EDGE_LENGTH);
            egc.strokeRectangle(groundMesh, 1.0, Color.RED);

            // Play ground
            egc.strokeRectangle(gameUiControl.getPlanetConfig().getPlayGround(), 1.0, Color.BLACK);
            // egc.strokeRectangle(new Rectangle2D(50, 40, 310, 320), 1.0, Color.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRender();
    }

    private void drawTerrain(ExtendedGraphicsContext egc) {
        // Ground mesh
        egc.strokeVertexList(terrainUiService.getGroundUi().getVertices(), 0.2, Color.BLUE);

        // Slopes
        for (SlopeUi slope : terrainUiService.getSlopes()) {
            egc.strokeVertexList(slope.getVertices(), 0.2, Color.RED);
        }

        // Terrain objects
//        egc.getGc().setFill(Color.GREEN);
//        for (Map.Entry<TerrainObjectConfig, Collection<TerrainObjectPosition>> entry : terrainUiService.getTerrainObjectPositions().getMap().entrySet()) {
//            TerrainObjectConfig terrainObjectConfig = entry.getKey();
//            for (TerrainObjectPosition terrainObjectPosition : entry.getValue()) {
//                egc.getGc().fillOval(terrainObjectPosition.getPosition().getX() - terrainObjectConfig.getRadius(),
//                        terrainObjectPosition.getPosition().getY() - terrainObjectConfig.getRadius(),
//                        2 * terrainObjectConfig.getRadius(), 2 * terrainObjectConfig.getRadius());
//            }
//
//        }

    }
}
