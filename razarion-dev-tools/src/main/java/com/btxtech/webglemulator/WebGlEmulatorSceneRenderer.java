package com.btxtech.webglemulator;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.DevToolUtil;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import javafx.scene.paint.Color;

import javax.inject.Inject;
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
    private Map<Index, UiTerrainTile> displayTerrainTiles;
    private Map<Index, UiTerrainTile> cacheTerrainTiles;

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

//            // Ground Mesh
//            Rectangle groundRect = gameUiControl.getPlanetConfig().getGroundMeshDimension();
//            Rectangle2D groundMesh = new Rectangle2D(groundRect.startX() * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH,
//                    groundRect.startY() * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH,
//                    groundRect.width() * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH,
//                    groundRect.height() * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
//            egc.strokeRectangle(groundMesh, 1.0, Color.RED);
//
//            // Play ground
//            egc.strokeRectangle(gameUiControl.getPlanetConfig().getPlayGround(), 1.0, Color.BLACK);
            // egc.strokeRectangle(new Rectangle2D(50, 40, 310, 320), 1.0, Color.BLACK);

            renderTerrainTiles(egc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRender();
    }

    private void drawTerrain(ExtendedGraphicsContext egc) {
//        // Ground mesh
//        egc.strokeVertexList(terrainUiService.getGroundUi().getVertices(), 0.2, Color.BLUE);
//
//        // Slopes
//        for (SlopeUi slope : terrainUiService.getSlopes()) {
//            egc.strokeVertexList(slope.getVertices(), 0.2, Color.RED);
//        }

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

    private void renderTerrainTiles(ExtendedGraphicsContext egc) {
        displayTerrainTiles = (Map<Index, UiTerrainTile>) DevToolUtil.readServiceFiled("displayTerrainTiles", terrainUiService);
        cacheTerrainTiles = (Map<Index, UiTerrainTile>) DevToolUtil.readServiceFiled("cacheTerrainTiles", terrainUiService);

        egc.getGc().setFill(Color.color(0.0, 1.0, 0.0, 0.5));
        for (UiTerrainTile active : displayTerrainTiles.values()) {
            Rectangle2D rectangle2D = TerrainUtil.toAbsoluteTileRectangle(new Index(active.getTerrainTile().getIndexX(), active.getTerrainTile().getIndexY()));
            egc.getGc().fillRect(rectangle2D.startX(), rectangle2D.startY(), rectangle2D.width() - 2, rectangle2D.height() - 2);
        }

        egc.getGc().setFill(Color.color(1.0, 0.0, 0.0, 0.5));
        for (UiTerrainTile active : cacheTerrainTiles.values()) {
            Rectangle2D rectangle2D = TerrainUtil.toAbsoluteTileRectangle(new Index(active.getTerrainTile().getIndexX(), active.getTerrainTile().getIndexY()));
            egc.getGc().fillRect(rectangle2D.startX(), rectangle2D.startY(), rectangle2D.width() - 2, rectangle2D.height() - 2);
        }


    }
}
