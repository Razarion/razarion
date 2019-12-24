package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeModeler {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    public static void sculpt(SlopeSkeletonConfig slopeSkeletonConfig, double[][] fractalField) {
        int segments = slopeSkeletonConfig.getSegments();
        Shape shape = new Shape(slopeSkeletonConfig.getSlopeShapes());
        int rows = shape.getVertexCount();

        SlopeNode[][] slopeNodes = new SlopeNode[segments][rows];
        // FractalField fractalField = FractalField.createFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
        for (int column = 0; column < segments; column++) {
            for (int row = 0; row < rows; row++) {
                SlopeNode slopeNode = new SlopeNode();
                slopeNode.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = 0;
                    if (fractalField != null) {
                        normShift = fractalField[column][row - shape.getShiftableOffset()];
                    }
                    slopeNode.setPosition(shape.getNormShiftedVertex(row, normShift));
                } else {
                    slopeNode.setPosition(shape.getVertex(row));
                }
                slopeNodes[column][row] = slopeNode;
            }
        }

        slopeSkeletonConfig.setSlopeNodes(slopeNodes);
        slopeSkeletonConfig.setWidth(shape.getDistance());
        slopeSkeletonConfig.setHeight(shape.getZInner());
        slopeSkeletonConfig.setSegments(segments);
        slopeSkeletonConfig.setRows(rows);
    }
}
