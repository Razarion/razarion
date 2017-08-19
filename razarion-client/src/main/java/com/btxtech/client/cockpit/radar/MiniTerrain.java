package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.utils.MathHelper;
import com.google.gwt.dom.client.Element;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Dependent
public class MiniTerrain extends AbstractMiniMap {
    private static final String WATER_COLOR = "#0000ff";
    private static final String GROUND_COLOR = "#86b300";
    private static final String SLOPE_COLOR = "#8c8c8c";
    private static final String TERRAIN_OBJECT_COLOR = "#008000";
    private Logger logger = Logger.getLogger(MiniTerrain.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;

    @Override
    public void init(Element canvasElement, int width, int height) {
        super.init(canvasElement, width, height);
    }

    public void generateMiniTerrain(Rectangle2D playground, List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        getCtx().save();
        scaleToPlayground(playground);

        // Ground
        getCtx().setFillStyle(GROUND_COLOR);
        getCtx().beginPath();
        getCtx().rect(0, 0, (float) playground.width(), (float) playground.height());
        getCtx().fill();

        // Slopes
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigId());
            switch (slopeSkeletonConfig.getType()) {
                case LAND:
                    drawPlateau(playground, terrainSlopePosition, slopeSkeletonConfig);
                    break;
                case WATER:
                    drawWater(playground, terrainSlopePosition);
                    break;
                default:
                    logger.warning("MiniTerrain.generateMiniTerrain() unknown slopeSkeletonConfig.getType(): " + slopeSkeletonConfig.getType());
            }
        }

        // Terrain objects
        getCtx().setFillStyle(TERRAIN_OBJECT_COLOR);
        for (TerrainObjectPosition terrainObjectPosition : terrainObjectPositions) {
            TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId());
            DecimalPosition center = terrainObjectPosition.getPosition().sub(playground.getStart());
            getCtx().beginPath();
            getCtx().arc((float) center.getX(), (float) center.getY(), (float) terrainObjectConfig.getRadius(), 0f, (float) MathHelper.ONE_RADIANT, true);
            getCtx().fill();
        }
        getCtx().restore();
    }

    private void drawPlateau(Rectangle2D playground, TerrainSlopePosition terrainSlopePosition, SlopeSkeletonConfig slopeSkeletonConfig) {
        getCtx().setStrokeStyle(SLOPE_COLOR);
        getCtx().setLineWidth((float) slopeSkeletonConfig.getWidth());

        getCtx().beginPath();
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition position = polygon.get(i).getPosition().sub(playground.getStart());
            if (i == 0) {
                getCtx().moveTo((float) position.getX(), (float) position.getY());
            } else {
                getCtx().lineTo((float) position.getX(), (float) position.getY());
            }
        }
        getCtx().stroke();
    }

    private void drawWater(Rectangle2D playground, TerrainSlopePosition terrainSlopePosition) {
        getCtx().setFillStyle(WATER_COLOR);

        getCtx().beginPath();
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition position = polygon.get(i).getPosition().sub(playground.getStart());
            if (i == 0) {
                getCtx().moveTo((float) position.getX(), (float) position.getY());
            } else {
                getCtx().lineTo((float) position.getX(), (float) position.getY());
            }
        }
        getCtx().closePath();
        getCtx().fill();

    }

    public String toDataURL(String type) {
        return getCanvasElement().toDataURL("image/jpeg");
    }
}
