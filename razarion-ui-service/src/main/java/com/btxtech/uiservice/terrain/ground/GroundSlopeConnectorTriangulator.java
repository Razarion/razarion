package com.btxtech.uiservice.terrain.ground;

import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;

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
    private boolean isSlopeInner;
    private final Polygon2D innerPolygon;
    private final Polygon2D outerPolygon;
    private int innerIndex;
    private int outerIndex;

    public GroundSlopeConnectorTriangulator(VertexList vertexList, List<VertexDataObject> groundLine, List<VertexDataObject> slopeLine, boolean isSlopeInner) {
        this.vertexList = vertexList;
        this.groundLine = groundLine;
        this.slopeLine = slopeLine;
        this.isSlopeInner = isSlopeInner;
        innerPolygon = new Polygon2D(Vertex.toXY(slopeLine));
        outerPolygon = new Polygon2D(Vertex.toXY(groundLine));
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
                        addTriangle(groundVertex, slopeLine.get(innerIndex + 1), slopeVertex);
                        innerIndex++;
                    }
                } else {
                    addTriangle(groundVertex, groundLine.get(outerIndex + 1), slopeVertex);
                    outerIndex++;
                }
            } else {
                if (isCrossing(slopeLine.get(innerIndex + 1), groundVertex)) {
                    if (isCrossing(groundLine.get(outerIndex + 1), slopeVertex)) {
                        logger.severe("Ignoring triangle");
                        innerIndex++;
                        // TODO here
                    } else {
                        addTriangle(groundVertex, groundLine.get(outerIndex + 1), slopeVertex);
                        outerIndex++;
                    }
                } else {
                    addTriangle(groundVertex, slopeLine.get(innerIndex + 1), slopeVertex);
                    innerIndex++;
                }
            }
        }
    }

    private boolean isCrossing(Vertex point1, Vertex point2) {
        Line line = new Line(point1.toXY(), point2.toXY());

        return innerPolygon.isLineCrossing(line) || outerPolygon.isLineCrossing(line);
    }

    private void addTriangle(VertexDataObject vertexA, VertexDataObject vertexB, VertexDataObject vertexC) {
        VertexDataObject vertexACorrected;
        VertexDataObject vertexBCorrected;
        if (isSlopeInner) {
            vertexACorrected = vertexA;
            vertexBCorrected = vertexB;
        } else {
            vertexACorrected = vertexB;
            vertexBCorrected = vertexA;
        }
        double zA = vertexACorrected.cross(vertexBCorrected, vertexC).getZ();
        double zB = vertexBCorrected.cross(vertexC, vertexACorrected).getZ();
        double zC = vertexC.cross(vertexACorrected, vertexBCorrected).getZ();

        vertexList.add(vertexACorrected, vertexACorrected.getNorm(), vertexACorrected.getTangent(), vertexACorrected.getSplatting(),
                vertexBCorrected, vertexBCorrected.getNorm(), vertexBCorrected.getTangent(), vertexBCorrected.getSplatting(),
                vertexC, vertexC.getNorm(), vertexC.getTangent(), vertexC.getSplatting());
    }


}
