package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeModeler {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    public static void sculpt(SlopeConfig slopeConfig, FractalFieldConfig fractalFieldConfig) {
        int segments = slopeConfig.getSlopeSkeletonConfig().getSegments();
        Shape shape = new Shape(slopeConfig.getShape());
        int rows = shape.getVertexCount();

        SlopeNode[][] slopeNodes = new SlopeNode[segments][rows];
        // FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
        for (int column = 0; column < segments; column++) {
            for (int row = 0; row < rows; row++) {
                SlopeNode slopeNode = new SlopeNode();
                slopeNode.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = 0;
                    if (fractalFieldConfig.getClampedFractalField() != null) {
                        normShift = fractalFieldConfig.getClampedFractalField()[column][row - shape.getShiftableOffset()];
                    }
                    slopeNode.setPosition(shape.getNormShiftedVertex(row, normShift));
                } else {
                    slopeNode.setPosition(shape.getVertex(row));
                }
                slopeNodes[column][row] = slopeNode;
            }
        }

        slopeConfig.getSlopeSkeletonConfig().setSlopeNodes(slopeNodes);
        slopeConfig.getSlopeSkeletonConfig().setWidth(shape.getDistance());
        slopeConfig.getSlopeSkeletonConfig().setHeight(shape.getZInner());
        slopeConfig.getSlopeSkeletonConfig().setSegments(segments);
        slopeConfig.getSlopeSkeletonConfig().setRows(rows);
    }
}
