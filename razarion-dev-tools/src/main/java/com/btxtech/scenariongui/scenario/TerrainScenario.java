package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.slope.AbstractBorder;
import com.btxtech.shared.gameengine.planet.terrain.slope.InnerCornerBorder;
import com.btxtech.shared.gameengine.planet.terrain.slope.LineBorder;
import com.btxtech.shared.gameengine.planet.terrain.slope.OuterCornerBorder;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TerrainScenario extends AbstractTerrainScenario {

    @Override
    public void render(ExtendedGraphicsContext context) {
        // TODO user TerrainTile instead context.strokeTriangles(getTerrainService().getGroundMesh().provideVertexList().getVertices(), 0.1, Color.BLUE);

//        for (Slope slope : getTerrainService().getSlopes()) {
//
//            try {
////            context.fillVertexList(slope.getGroundPlateauConnector().getInnerConnectionVertexList().OLDgetVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
//                context.fillVertexList(slope.getGroundPlateauConnector().getOuterConnectionVertexList().getVertices(), 0.1, Color.RED);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//
//            context.strokeTriangles(slope.getMesh().getVertices(), 0.02, Color.PINK);
//
//            try {
////                context.strokeCurve(slope.getGroundPlateauConnector().getOuterGroundEdges(), 0.1, Color.GREEN, true);
////            context.strokeCurve(slope.getGroundPlateauConnector().getOuterSlopeEdges(), 1, Color.RED, false);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//
//            if (!slope.hasWater()) {
//                context.strokeTriangles(slope.getGroundPlateauConnector().getTopMesh().provideVertexList().getVertices(), 0.1, Color.BLUEVIOLET);
//                context.strokeCurve(slope.getGroundPlateauConnector().getInnerGroundEdges(), 0.1, Color.GREEN, true);
//                context.strokeCurve(slope.getGroundPlateauConnector().getInnerSlopeEdges(), 0.1, Color.RED, true);
//                context.fillVertexList(slope.getGroundPlateauConnector().getInnerConnectionVertexList().getVertices(), 0.1, Color.RED);
//            }
//
//            context.strokeCurveDecimalPosition(slope.getCorner2d(), 0.01, Color.BLACK, true);
//
//            context.getGc().setLineWidth(0.02);
//            for (AbstractBorder abstractBorder : slope.getBorders()) {
//                if (abstractBorder instanceof LineBorder) {
//                    context.getGc().setStroke(Color.BLUE);
//                    LineBorder lineBorder = (LineBorder) abstractBorder;
//                    context.getGc().beginPath();
//                    context.getGc().moveTo(lineBorder.getInnerStart().getX(), lineBorder.getInnerStart().getY());
//                    context.getGc().lineTo(lineBorder.getInnerEnd().getX(), lineBorder.getInnerEnd().getY());
//                    context.getGc().lineTo(lineBorder.getOuterEnd().getX(), lineBorder.getOuterEnd().getY());
//                    context.getGc().lineTo(lineBorder.getOuterStart().getX(), lineBorder.getOuterStart().getY());
//                    context.getGc().closePath();
//                    context.getGc().stroke();
//                } else if (abstractBorder instanceof InnerCornerBorder) {
//                    context.getGc().setStroke(Color.GREEN);
//                    InnerCornerBorder innerCornerBorder = (InnerCornerBorder) abstractBorder;
//                    context.getGc().beginPath();
//                    context.getGc().moveTo(innerCornerBorder.getInnerStart().getX(), innerCornerBorder.getInnerStart().getY());
//                    context.getGc().lineTo(innerCornerBorder.getOuterStart().getX(), innerCornerBorder.getOuterStart().getY());
//                    context.getGc().lineTo(innerCornerBorder.getOuterEnd().getX(), innerCornerBorder.getOuterEnd().getY());
//                    context.getGc().closePath();
//                    context.getGc().stroke();
//                } else if (abstractBorder instanceof OuterCornerBorder) {
//                    context.getGc().setStroke(Color.BROWN);
//                    OuterCornerBorder outerCornerBorder = (OuterCornerBorder) abstractBorder;
//                    context.getGc().beginPath();
//                    context.getGc().moveTo(outerCornerBorder.getInnerEnd().getX(), outerCornerBorder.getInnerEnd().getY());
//                    context.getGc().lineTo(outerCornerBorder.getInnerStart().getX(), outerCornerBorder.getInnerStart().getY());
//                    context.getGc().lineTo(outerCornerBorder.getOuterStart().getX(), outerCornerBorder.getOuterStart().getY());
//                    context.getGc().closePath();
//                    context.getGc().stroke();
//                }
//            }
//
//        }
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        // terrainUiService.getInterpolatedTerrainTriangle(new DecimalPosition(position));
        return false;
    }
}
