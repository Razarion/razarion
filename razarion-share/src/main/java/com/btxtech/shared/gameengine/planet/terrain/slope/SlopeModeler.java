package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import java.util.List;


/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeModeler {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    public static CalculatedSlopeData sculpt(SlopeConfig slopeConfig, double height) {

//        double[][] fractalField = ???
//        SlopeNode[][] slopeNodes = new SlopeNode[segments][rows];
//        // FractalField fractalField = FractalField.createFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
//        for (int column = 0; column < segments; column++) {
//            for (int row = 0; row < rows; row++) {
//                SlopeNode slopeNode = new SlopeNode();
//                slopeNode.setSlopeFactor(shape.getSlopeFactor(row));
//                if (shape.isShiftableEntry(row)) {
//                    double normShift = 0;
//                    if (fractalField != null) {
//                        normShift = fractalField[column][row - shape.getShiftableOffset()];
//                    }
//                    slopeNode.setPosition(shape.getNormShiftedVertex(row, normShift));
//                } else {
//                    slopeNode.setPosition(shape.getVertex(row));
//                }
//                slopeNodes[column][row] = slopeNode;
//            }
//        }
//
//        slopeConfig.setSlopeNodes(slopeNodes);

        List<SlopeShape> slopeShapes = slopeConfig.getSlopeShapes();

        return new CalculatedSlopeData()
                .slopeShapes(slopeShapes)
                .width(Math.abs(slopeShapes.get(slopeShapes.size() - 1).getPosition().getX()))
                .height(height)
                .rows(slopeShapes.size());
    }
}
