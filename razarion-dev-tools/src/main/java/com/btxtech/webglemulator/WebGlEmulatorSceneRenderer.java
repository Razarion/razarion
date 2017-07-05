package com.btxtech.webglemulator;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.DevToolUtil;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
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

            // Camera view filed
            ViewField cameraView = projectionTransformation.calculateViewField(0);
            if (!cameraView.hasNullPosition()) {
                egc.strokePolygon(cameraView.toList(), 2.0, Color.BLACK, false);
            }

            renderTerrainTiles(egc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRender();
    }

    private void renderTerrainTiles(ExtendedGraphicsContext egc) {
        displayTerrainTiles = (Map<Index, UiTerrainTile>) DevToolUtil.readServiceFiled("displayTerrainTiles", terrainUiService);
        cacheTerrainTiles = (Map<Index, UiTerrainTile>) DevToolUtil.readServiceFiled("cacheTerrainTiles", terrainUiService);

        for (UiTerrainTile active : displayTerrainTiles.values()) {
            Rectangle2D rectangle2D = TerrainUtil.toAbsoluteTileRectangle(new Index(active.getTerrainTile().getIndexX(), active.getTerrainTile().getIndexY()));
            // renderIsFree(egc, active, rectangle2D.getStart());
            egc.getGc().setFill(Color.color(0.0, 1.0, 0.0, 0.5));
            egc.getGc().fillRect(rectangle2D.startX(), rectangle2D.startY(), rectangle2D.width() - 2, rectangle2D.height() - 2);
            renderHeight(egc, active, rectangle2D.getStart());
        }

        for (UiTerrainTile active : cacheTerrainTiles.values()) {
            Rectangle2D rectangle2D = TerrainUtil.toAbsoluteTileRectangle(new Index(active.getTerrainTile().getIndexX(), active.getTerrainTile().getIndexY()));
            //renderIsFree(egc, active, rectangle2D.getStart());
            egc.getGc().setFill(Color.color(1.0, 0.0, 0.0, 0.5));
            egc.getGc().fillRect(rectangle2D.startX(), rectangle2D.startY(), rectangle2D.width() - 2, rectangle2D.height() - 2);
        }
    }

    private void renderIsFree(ExtendedGraphicsContext egc, UiTerrainTile uiTerrainTile, DecimalPosition offset) {
        for (double x = 0; x < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; x++) {
            for (double y = 0; y < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; y++) {
                DecimalPosition terrainPosition = new DecimalPosition(x, y).add(offset);
                if (!uiTerrainTile.isTerrainFree(terrainPosition.add(0.5, 0.5))) {
                    egc.getGc().setFill(Color.color(0, 0, 0, 1));
                    egc.getGc().fillRect(terrainPosition.getX(), terrainPosition.getY(), 1, 1);
                }
            }
        }
    }

    private void renderHeight(ExtendedGraphicsContext egc, UiTerrainTile uiTerrainTile, DecimalPosition offset) {
        for (double x = 0; x < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; x++) {
            for (double y = 0; y < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; y++) {
                DecimalPosition terrainPosition = new DecimalPosition(x, y).add(offset);
                double height = uiTerrainTile.interpolateDisplayHeight(terrainPosition.add(0.5, 0.5));
                double v = (height + 10) / 20;
                egc.getGc().setFill(Color.color(v, v, v, 1));
                egc.getGc().fillRect(terrainPosition.getX(), terrainPosition.getY(), 1, 1);
            }
        }
    }
}
