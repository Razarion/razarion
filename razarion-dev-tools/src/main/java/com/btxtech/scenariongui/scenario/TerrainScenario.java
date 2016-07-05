package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.GameMock;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.uiservice.terrain.slope.Slope;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TerrainScenario extends Scenario {
    private TerrainSurface terrainSurface;

    @Override
    public void render(ExtendedGraphicsContext context) {
        terrainSurface = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");
        context.strokeVertexList(terrainSurface.getGroundVertexList().getVertices(), 0.1, Color.BLUE);

        for (Integer slopeId : terrainSurface.getSlopeIds()) {
            Slope slope = terrainSurface.getSlope(slopeId);


            // context.fillVertexList(slope.getGroundPlateauConnector().getInnerConnectionVertexList().getVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
            // context.fillVertexList(slope.getGroundPlateauConnector().getOuterConnectionVertexList().getVertices(), 2, Color.RED);

            context.strokeVertexList(slope.getMesh().getVertices(), 0.1, Color.PINK);


//            context.strokeCurve(slope.getGroundPlateauConnector().getOuterGroundEdges(), 1, Color.GREEN, true);
//            context.strokeCurve(slope.getGroundPlateauConnector().getOuterSlopeEdges(), 1, Color.RED, false);

            if (!slope.hasWater()) {
                context.strokeCurve(slope.getGroundPlateauConnector().getInnerGroundEdges(), 0.1, Color.GREEN, true);
                context.strokeCurve(slope.getGroundPlateauConnector().getInnerSlopeEdges(), 0.1, Color.RED, true);
            }


        }
    }

    @Override
    public boolean onMouseDown(Index position) {
        terrainSurface.getInterpolatedTerrainTriangle(new DecimalPosition(position));
        return false;
    }
}
