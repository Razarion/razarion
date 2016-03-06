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
                } else {
                    shapeTemplateEntry.setPosition(shape.getVertex(row));
                }
                nodes[column][correctedRow] = shapeTemplateEntry;
            }
        }
    }

    public void generateMesh(Mesh mesh, List<AbstractBorder> skeleton, List<Vertex> innerLine, List<Vertex> outerLine) {
        int templateColumn = 0;
        int meshColumn = 0;
        for (AbstractBorder abstractBorder : skeleton) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < rows; row++) {
                    ShapeTemplateEntry shapeTemplateEntry = nodes[templateColumn][row];
                    Vertex transformedPoint = transformationMatrix.multiply(shapeTemplateEntry.getPosition(), 1.0);
                    mesh.addVertex(meshColumn, row, transformedPoint, shapeTemplateEntry.getSlopeFactor());
                    if (row == 0) {
                        outerLine.add(transformedPoint);
                    } else if (row + 1 == rows) {
                        innerLine.add(transformedPoint);
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

    public double getDistance() {
        return shape.getDistance();
    }

    public Shape getShape() {
        return shape;
    }

}
