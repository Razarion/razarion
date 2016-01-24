package com.btxtech.client.terrain.slope;

import com.btxtech.client.terrain.FractalField;
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
    private Vertex[][] nodes;

    public ShapeTemplate(int columns, Shape shape) {
        this.columns = columns;
        this.shape = shape;
        rows = shape.getVertexCount();
        nodes = new Vertex[columns][shape.getVertexCount()];
    }

    public void sculpt(double shift, double roughness) {
        FractalField fractalField = FractalField.createSaveFractalField(shape.getShiftableVertexCount(), columns, roughness, -shift / 2.0, shift / 2.0);
        fractalField.process();
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < shape.getVertexCount(); row++) {
                if (shape.isShiftableVertex(row)) {
                    double normShift = fractalField.getValue(column, row);
                    nodes[column][row] = shape.getNormShiftedVertex(row, normShift);
                } else {
                    nodes[column][row] = shape.getVertex(row);
                }
            }
        }
    }

    public void generateMesh(Mesh mesh, List<AbstractBorder> skeleton) {
        int templateColumn = 0;
        int meshColumn = 0;
        for (AbstractBorder abstractBorder : skeleton) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < rows; row++) {
                    Vertex templateVector = nodes[templateColumn][row];
                    Vertex transformedPoint = transformationMatrix.multiply(templateVector, 1.0);
                    // TODO calculate slope factor
                    mesh.addVertex(meshColumn, row, transformedPoint, 1f);
                }
                templateColumn++;
                if (templateColumn >= columns) {
                    templateColumn = 0;
                }
                meshColumn++;
            }
        }
    }

    public double getDistance() {
        return shape.getDistance();
    }

    public Shape getShape() {
        return shape;
    }

}
