package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.GameMock;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.uiservice.terrain.slope.AbstractBorder;
import com.btxtech.uiservice.terrain.slope.Slope;
import com.btxtech.uiservice.terrain.slope.VerticalSegment;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TerrainScenario extends Scenario {
    @Override
    public void render(ExtendedGraphicsContext context) {
        TerrainSurface terrainSurface = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");

        for (Integer slopeId : terrainSurface.getSlopeIds()) {
            Slope slope = terrainSurface.getSlope(slopeId);

            // context.strokeCurve(slope.getGroundPlateauConnector().getInnerGroundEdges(), 1, Color.GREEN, true);
            // context.strokeCurve(slope.getGroundPlateauConnector().getInnerSlopeEdges(), 1, Color.RED, true);

            // context.fillVertexList(slope.getGroundPlateauConnector().getInnerConnectionVertexList().getVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
            // context.fillVertexList(slope.getGroundPlateauConnector().getOuterConnectionVertexList().getVertices(), 2, Color.RED);

            context.strokeVertexList(slope.getMesh().getVertices(), 1, Color.PINK);

            List<Index> innerBorder = new ArrayList<>();
            List<Index> outerBorder = new ArrayList<>();
            for (AbstractBorder abstractBorder : slope.getBorders()) {
                for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                    innerBorder.add(verticalSegment.getInner());
                    outerBorder.add(verticalSegment.getOuter());
                }
            }
            context.strokeCurveIndex(innerBorder, 1, Color.BROWN, true);
            context.strokeCurveIndex(outerBorder, 1, Color.DARKCYAN, true);


//            context.strokeCurve(slope.getGroundPlateauConnector().getOuterGroundEdges(), 1, Color.GREEN, true);
//            context.strokeCurve(slope.getGroundPlateauConnector().getOuterSlopeEdges(), 1, Color.RED, false);
        }
        context.strokeVertexList(terrainSurface.getGroundVertexList().getVertices(), 0.1, Color.BLUE);
    }
}
