package com.btxtech.client.terrain.slope;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.FractalField;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class ShapeTemplate {
    private int rows;
    private int columns;
    private final Shape shape;
    // columns, row
    private ShapeTemplateEntry[][] nodes;

    public ShapeTemplate(int columns, Shape shape) {
        this.columns = columns;
        this.shape = shape;
        rows = shape.getVertexCount();
        nodes = new ShapeTemplateEntry[columns][shape.getVertexCount()];
    }

    public void sculpt(double shift, double roughness) {
        FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableCount(), columns, roughness, -shift / 2.0, shift / 2.0);
        fractalField.process();
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int correctedRow = rows - 1 - row;
                ShapeTemplateEntry shapeTemplateEntry = new ShapeTemplateEntry();
                shapeTemplateEntry.setSlopeFactor(shape.getSlopeFactor(row));
                if (shape.isShiftableEntry(row)) {
                    double normShift = fractalField.getValue(column, correctedRow - shape.getShiftableOffset());
                    shapeTemplateEntry.setPosition(shape.getNormShiftedVertex(row, normShift));
                    shapeTemplateEntry.setNormShift(normShift);
                } else {
                    shapeTemplateEntry.setPosition(shape.getVertex(row));
                    shapeTemplateEntry.setNormShift(0);
                }
                nodes[column][correctedRow] = shapeTemplateEntry;
            }
        }
    }

    public void generateMesh(Mesh mesh, List<AbstractBorder> skeleton, List<Index> innerLineMeshIndex, List<Index> outerLineMeshIndex, GroundMesh groundMeshSplatting) {
        int templateColumn = 0;
        int meshColumn = 0;
        for (AbstractBorder abstractBorder : skeleton) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < rows; row++) {
                    ShapeTemplateEntry shapeTemplateEntry = nodes[templateColumn][row];
                    Vertex transformedPoint = transformationMatrix.multiply(shapeTemplateEntry.getPosition(), 1.0);
                    float splatting = setupSplatting(transformedPoint, shapeTemplateEntry.getSlopeFactor(), groundMeshSplatting);
                    mesh.addVertex(meshColumn, row, transformedPoint, setupSlopeFactor(shapeTemplateEntry), splatting);
                    if (row == 0) {
                        outerLineMeshIndex.add(new Index(meshColumn, row));
                    } else if (row + 1 == rows) {
                        innerLineMeshIndex.add(new Index(meshColumn, row));
                    }
                }
                templateColumn++;
                if (templateColumn >= columns) {
                    templateColumn = 0;
                }
                meshColumn++;
            }
        }
    }

    private float setupSlopeFactor(ShapeTemplateEntry shapeTemplateEntry) {
        if (MathHelper.compareWithPrecision(1.0, shapeTemplateEntry.getSlopeFactor())) {
            return 1;
        } else if (MathHelper.compareWithPrecision(0.0, shapeTemplateEntry.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        return (float) MathHelper.clamp(shapeTemplateEntry.getSlopeFactor() - shapeTemplateEntry.getNormShift(), 0.0, 1.0);
    }

    private float setupSplatting(Vertex vertex, float slopeFactor, GroundMesh groundMesh) {
        if (MathHelper.compareWithPrecision(1.0, slopeFactor)) {
            return 0;
        } else {
            return (float) groundMesh.getInterpolatedSplatting(vertex.toXY());
        }
    }

    public double getDistance() {
        return shape.getDistance();
    }

    public Shape getShape() {
        return shape;
    }
}
