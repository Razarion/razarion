package com.btxtech.uiservice.terrain.ground;

import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.06.2016.
 */
public class GroundSlopeConnectorTriangulator {
    private Logger logger = Logger.getLogger(GroundSlopeConnectorTriangulator.class.getName());
    private final VertexList vertexList;
    private final List<VertexDataObject> groundLine;
    private final List<VertexDataObject> slopeLine;
    private final Polygon2D innerPolygon;
    private final Polygon2D outerPolygon;
    private int innerIndex;
    private int outerIndex;

    public GroundSlopeConnectorTriangulator(VertexList vertexList, List<VertexDataObject> groundLine, List<VertexDataObject> slopeLine, boolean isSlopeInner) {
        this.vertexList = vertexList;
        this.groundLine = groundLine;
        this.slopeLine = slopeLine;
        innerPolygon = new Polygon2D(Vertex.toXY(isSlopeInner ? slopeLine : groundLine));
        outerPolygon = new Polygon2D(Vertex.toXY(isSlopeInner ? groundLine : slopeLine));
    }

    public void triangulation() {
        groundLine.add(groundLine.get(0));
        slopeLine.add(slopeLine.get(0));

        while (outerIndex + 1 < groundLine.size() || innerIndex + 1 < slopeLine.size()) {
            VertexDataObject groundVertex = groundLine.get(outerIndex);
            VertexDataObject slopeVertex = slopeLine.get(innerIndex);

            double distanceOuter = Double.MAX_VALUE;
            if (outerIndex + 1 < groundLine.size()) {
                distanceOuter = groundLine.get(outerIndex + 1).distance(slopeVertex);
            }

            double distanceInner = Double.MAX_VALUE;
            if (innerIndex + 1 < slopeLine.size()) {
                distanceInner = slopeLine.get(innerIndex + 1).distance(groundVertex);
            }

            if (distanceOuter < distanceInner) {
                if (isCrossing(groundLine.get(outerIndex + 1), slopeVertex)) {
                    if (isCrossing(slopeLine.get(innerIndex + 1), groundVertex)) {
                        logger.severe("Ignoring triangle");
                        // TODO here
                        outerIndex++;
                    } else {
                        addOuterTriangle(groundVertex, slopeLine.get(innerIndex + 1), slopeVertex);
                        innerIndex++;
                    }
                } else {
                    addOuterTriangle(groundVertex, groundLine.get(outerIndex + 1), slopeVertex);
                    outerIndex++;
                }
            } else {
                if (isCrossing(slopeLine.get(innerIndex + 1), groundVertex)) {
                    if (isCrossing(groundLine.get(outerIndex + 1), slopeVertex)) {
                        logger.severe("Ignoring triangle");
                        innerIndex++;
                        // TODO here
                    } else {
                        addOuterTriangle(groundVertex, groundLine.get(outerIndex + 1), slopeVertex);
                        outerIndex++;
                    }
                } else {
                    addOuterTriangle(groundVertex, slopeLine.get(innerIndex + 1), slopeVertex);
                    innerIndex++;
                }
            }
        }
    }

    private boolean isCrossing(Vertex point1, Vertex point2) {
        Line line = new Line(point1.toXY(), point2.toXY());

        return innerPolygon.isLineCrossing(line) || outerPolygon.isLineCrossing(line);
    }

    private void addOuterTriangle(VertexDataObject vertexA, VertexDataObject vertexB, VertexDataObject vertexC) {
        vertexList.add(vertexA, vertexA.getNorm(), vertexA.getTangent(), vertexA.getSplatting(),
                vertexB, vertexB.getNorm(), vertexB.getTangent(), vertexB.getSplatting(),
                vertexC, vertexC.getNorm(), vertexC.getTangent(), vertexC.getSplatting());
    }


}
