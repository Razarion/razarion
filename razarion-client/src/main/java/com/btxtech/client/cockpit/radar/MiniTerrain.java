package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.google.gwt.dom.client.Element;

import java.util.List;

/**
 * Created by Beat
 * on 16.06.2017.
 */
public class MiniTerrain extends AbstractMiniMap {
    public MiniTerrain(Element canvasElement, int width, int height) {
        super(canvasElement, width, height);
    }

    public void generateMiniTerrain(Rectangle2D playground, List<TerrainSlopePosition> terrainSlopePositions) {
        scaleToPlayground(playground);

        getCtx().setFillStyle("#f00");

        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            getCtx().beginPath();

            List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
            for (int i = 0; i < polygon.size(); i++) {
                DecimalPosition position = polygon.get(i).getPosition().sub(playground.getStart());
                if(i == 0) {
                    getCtx().moveTo((float)position.getX(), (float)position.getY());
                } else {
                    getCtx().lineTo((float)position.getX(), (float)position.getY());
                }
            }
            getCtx().closePath();
            getCtx().fill();
        }
    }
}
