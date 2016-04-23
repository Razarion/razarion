package com.btxtech.client.terrain.slope.skeleton;

import com.btxtech.client.terrain.FractalField;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeSkeletonFactory {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    private int rows;
    private int segments;
    private final Shape shape;

    public SlopeSkeletonFactory(int segments, Shape shape) {
        this.segments = segments;
        this.shape = shape;
        rows = shape.getVertexCount();
    }

    public SlopeSkeleton sculpt(double shift, double roughness) {
        SlopeSkeletonEntry[][] nodes = new SlopeSkeletonEntry[segments][shape.getVertexCount()];
        FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
        fractalField.process();
        for (int column = 0; column < segments; column++) {
            for (int row = 0; row < rows; row++) {
                int correctedRow = rows - 1 - row;
                SlopeSkeletonEntry slopeSkeletonEntry = new SlopeSkeletonEntry();
                slopeSkeletonEntry.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = fractalField.getValue(column, correctedRow - shape.getShiftableOffset());
                    slopeSkeletonEntry.setPosition(shape.getNormShiftedVertex(row, normShift));
                    slopeSkeletonEntry.setNormShift(normShift);
                } else {
                    slopeSkeletonEntry.setPosition(shape.getVertex(row));
                    slopeSkeletonEntry.setNormShift(0);
                }
                nodes[column][correctedRow] = slopeSkeletonEntry;
            }
        }
        return new SlopeSkeleton(nodes, (int)shape.getDistance(), (int)shape.getZInner());
    }
}
