package com.btxtech.client.terrain.slope.skeleton;

import com.btxtech.client.terrain.FractalField;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.shared.SlopeSkeletonEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeSkeletonFactory {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    public static void sculpt(SlopeConfigEntity slopeConfigEntity) {
        int segments = slopeConfigEntity.getSegments();
        Shape shape = new Shape(slopeConfigEntity.getShape());
        int rows = shape.getVertexCount();
        double shift = slopeConfigEntity.getFractalShift();
        double roughness = slopeConfigEntity.getFractalRoughness();

        List<SlopeSkeletonEntry> slopeSkeletonEntries = new ArrayList<>();
        FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
        fractalField.process();
        for (int column = 0; column < segments; column++) {
            for (int row = 0; row < rows; row++) {
                int correctedRow = rows - 1 - row;
                SlopeSkeletonEntry slopeSkeletonEntry = new SlopeSkeletonEntry(column, correctedRow);
                slopeSkeletonEntry.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = fractalField.getValue(column, correctedRow - shape.getShiftableOffset());
                    slopeSkeletonEntry.setPosition(shape.getNormShiftedVertex(row, normShift));
                    slopeSkeletonEntry.setNormShift(normShift);
                } else {
                    slopeSkeletonEntry.setPosition(shape.getVertex(row));
                    slopeSkeletonEntry.setNormShift(0);
                }
                slopeSkeletonEntries.add(slopeSkeletonEntry);
            }
        }

        SlopeSkeletonEntity slopeSkeletonEntity = slopeConfigEntity.getSlopeSkeletonEntity();
        if (slopeSkeletonEntity == null) {
            slopeSkeletonEntity = new SlopeSkeletonEntity();
            slopeConfigEntity.setSlopeSkeletonEntity(slopeSkeletonEntity);
        }
        slopeSkeletonEntity.setValues(slopeSkeletonEntries, (int) shape.getDistance(), (int) shape.getZInner(), segments, rows);
    }
}
