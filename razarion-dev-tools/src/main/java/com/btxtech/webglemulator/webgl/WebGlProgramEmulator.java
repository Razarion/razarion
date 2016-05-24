package com.btxtech.webglemulator.webgl;

import javafx.scene.paint.Paint;

import java.util.List;

/**
 * Created by Beat
 * 24.05.2016.
 */
public class WebGlProgramEmulator {
    private final VertexShader vertexShader;
    private final List<Double> doubles;
    private Paint paint;

    public WebGlProgramEmulator(VertexShader vertexShader, List<Double> doubles, Paint paint) {
        if (doubles == null || doubles.isEmpty()) {
            throw new IllegalArgumentException("Buffer is empty");
        }
        if (doubles.size() % 9 != 0) {
            throw new IllegalArgumentException("Buffer must be a multiple of 9 because there are 3 corners with x,y,z in a triangle: " + doubles.size());
        }
        if (vertexShader == null) {
            throw new IllegalStateException("Vertex has not been set");
        }
        this.vertexShader = vertexShader;
        this.doubles = doubles;
        this.paint = paint;
    }

    public int bufferSize() {
        return doubles.size();
    }

    public double getBufferElement(int index) {
        return doubles.get(index);
    }

    public VertexShader getVertexShader() {
        return vertexShader;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
}
