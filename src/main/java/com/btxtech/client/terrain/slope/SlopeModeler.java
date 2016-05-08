package com.btxtech.client.terrain.slope;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.FractalField;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.Shape;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class SlopeModeler {
    // private Logger logger = Logger.getLogger(ShapeTemplate.class.getName());
    public static void sculpt(SlopeConfig slopeConfig) {
        int segments = slopeConfig.getSlopeSkeleton().getSegments();
        Shape shape = new Shape(slopeConfig.getShape());
        int rows = shape.getVertexCount();
        double shift = slopeConfig.getFractalShift();
        double roughness = slopeConfig.getFractalRoughness();

        SlopeNode[][] slopeNodes = new SlopeNode[segments][rows];
        FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableCount(), segments, roughness, -shift / 2.0, shift / 2.0);
        for (int column = 0; column < segments; column++) {
            for (int row = 0; row < rows; row++) {
                SlopeNode slopeNode = new SlopeNode();
                slopeNode.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = fractalField.getValue(column, row - shape.getShiftableOffset());
                    slopeNode.setPosition(shape.getNormShiftedVertex(row, normShift));
                    slopeNode.setNormShift(normShift);
                } else {
                    slopeNode.setPosition(shape.getVertex(row));
                    slopeNode.setNormShift(0);
                }
                slopeNodes[column][row] = slopeNode;
            }
        }

        slopeConfig.getSlopeSkeleton().setSlopeNodes(slopeNodes);
        slopeConfig.getSlopeSkeleton().setWidth((int) shape.getDistance());
        slopeConfig.getSlopeSkeleton().setHeight((int) shape.getZInner());
        slopeConfig.getSlopeSkeleton().setSegments(segments);
        slopeConfig.getSlopeSkeleton().setRows(rows);
    }

    public static void generateMesh(Mesh mesh, SlopeSkeleton slopeSkeleton, List<AbstractBorder> borders, List<Index> innerLineMeshIndex, List<Index> outerLineMeshIndex, GroundMesh groundMesh) {
        int templateSegment = 0;
        int meshColumn = 0;
        for (AbstractBorder border : borders) {
            for (VerticalSegment verticalSegment : border.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < slopeSkeleton.getRows(); row++) {
                    SlopeNode slopeNode = slopeSkeleton.getSlopeNodes()[templateSegment][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeNode.getPosition(), 1.0);
                    float splatting = setupSplatting(transformedPoint, slopeNode.getSlopeFactor(), groundMesh);
                    mesh.addVertex(meshColumn, row, transformedPoint, setupSlopeFactor(slopeNode), splatting);
                    if (row == 0) {
                        outerLineMeshIndex.add(new Index(meshColumn, row));
                    } else if (row + 1 == slopeSkeleton.getRows()) {
                        innerLineMeshIndex.add(new Index(meshColumn, row));
                    }
                }
                templateSegment++;
                if (templateSegment >= slopeSkeleton.getSegments()) {
                    templateSegment = 0;
                }
                meshColumn++;
            }
        }
    }

    private static float setupSlopeFactor(SlopeNode slopeNode) {
        if (MathHelper.compareWithPrecision(1.0, slopeNode.getSlopeFactor())) {
            return 1;
        } else if (MathHelper.compareWithPrecision(0.0, slopeNode.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return (float)slopeNode.getSlopeFactor();
    }

    private static float setupSplatting(Vertex vertex, double slopeFactor, GroundMesh groundMesh) {
        return (float) groundMesh.getInterpolatedSplatting(vertex.toXY());
    }

}
