package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeShape;

import java.util.List;

public class CalculatedSlopeData {
    private List<SlopeShape> slopeShapes;
    private double width;
    private double height;
    private int rows;

    public List<SlopeShape> getSlopeShapes() {
        return slopeShapes;
    }

    public void setSlopeShapes(List<SlopeShape> slopeShapes) {
        this.slopeShapes = slopeShapes;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public CalculatedSlopeData slopeShapes(List<SlopeShape> slopeShapes) {
        setSlopeShapes(slopeShapes);
        return this;
    }

    public CalculatedSlopeData width(double width) {
        setWidth(width);
        return this;
    }

    public CalculatedSlopeData height(double height) {
        setHeight(height);
        return this;
    }

    public CalculatedSlopeData rows(int rows) {
        setRows(rows);
        return this;
    }

    public Vertex setupVertex(int column, int row) {
        DecimalPosition xzPosition = slopeShapes.get(row).getPosition();
        if (row == 0) {
            return new Vertex(0, 0, 0);
        } else {
            return new Vertex(xzPosition.getX(), 0, xzPosition.getY());
        }
    }

    public double setupSlopeFactor(int column, int row) {
        return slopeShapes.get(row).getSlopeFactor();
    }
}
