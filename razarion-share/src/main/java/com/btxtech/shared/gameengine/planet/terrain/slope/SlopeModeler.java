package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.utils.MathHelper;

import java.util.List;

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

    public static void generateMesh(Mesh mesh, SlopeSkeletonConfig slopeSkeletonConfig, List<AbstractBorder> borders, List<Index> innerLineMeshIndex, List<Index> outerLineMeshIndex, GroundMesh groundMesh) {
        int templateSegment = 0;
        int meshColumn = 0;

        DecimalPosition lastInnerBorder = null;
        Vertex lastInnerVertex = null;
        for (AbstractBorder border : borders) {
            for (VerticalSegment verticalSegment : border.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < slopeSkeletonConfig.getRows(); row++) {
                    Vertex preCalculatedVertex = null;
                    if (row + 1 == slopeSkeletonConfig.getRows()) {
                        // Fix for imprecision of inner border
                        if (lastInnerBorder != null && lastInnerBorder.equals(verticalSegment.getInner())) {
                            preCalculatedVertex = lastInnerVertex;
                        }
                    }
                    SlopeNode slopeNode = slopeSkeletonConfig.getSlopeNodes()[templateSegment][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeNode.getPosition(), 1.0);
                    InterpolatedTerrainTriangle terrainTriangle = groundMesh.getInterpolatedTerrainTriangle(transformedPoint.toXY());
                    if (terrainTriangle != null) {
                        if (preCalculatedVertex == null) {
                            mesh.addVertex(meshColumn, row, transformedPoint, setupSlopeFactor(slopeNode), (float) terrainTriangle.getSplatting());
                            if (row == 0) {
                                outerLineMeshIndex.add(new Index(meshColumn, row));
                            } else if (row + 1 == slopeSkeletonConfig.getRows()) {
                                innerLineMeshIndex.add(new Index(meshColumn, row));
                                lastInnerBorder = verticalSegment.getInner();
                                lastInnerVertex = transformedPoint;
                            }
                        } else {
                            mesh.addVertex(meshColumn, row, preCalculatedVertex, setupSlopeFactor(slopeNode), (float) terrainTriangle.getSplatting());
                        }
                    }
                }
                templateSegment++;
                if (templateSegment >= slopeSkeletonConfig.getSegments()) {
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
        return (float) slopeNode.getSlopeFactor();
    }
}
