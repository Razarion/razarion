package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.GameMock;
import com.btxtech.client.terrain.GroundMesh;
import com.btxtech.client.terrain.GroundSlopeConnector;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.Water;
import com.btxtech.client.terrain.slope.AbstractBorder;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.client.terrain.slope.VerticalSegment;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Polygon2I;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TerrainScenario extends Scenario {
    @Override
    public void render(ExtendedGraphicsContext context) {
        TerrainSurface terrainSurface = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");

        Slope slope = terrainSurface.getSlope(0);


        // context.strokeCurve(slope.getGroundPlateauConnector().getInnerGroundEdges(), 1, Color.GREEN, true);
        // context.strokeCurve(slope.getGroundPlateauConnector().getInnerSlopeEdges(), 1, Color.RED, true);

        // context.strokeCurveIndex(terrainSlopePosition.getPolygon(), 1.0, Color.BLACK, true);

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


        // context.strokeVertexList(terrainSurface.getGroundVertexList().getVertices(), 0.1, Color.BLUE);

        // context.strokeCurve(slope.getGroundPlateauConnector().getOuterGroundEdges(), 1, Color.GREEN, true);
        // context.strokeCurve(slope.getGroundPlateauConnector().getOuterSlopeEdges(), 1, Color.RED, false);
    }
}
