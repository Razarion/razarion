package com.btxtech.webglemulator.webgl;

import javafx.scene.paint.Paint;

import java.util.List;

/**
 * Created by Beat
 * 24.05.2016.
 */
public class WebGlProgramEmulator {
    private VertexShader vertexShader;
    private List<Double> doubles;
    private Paint paint;
    private RenderMode renderMode;

    public WebGlProgramEmulator() {
    }

    @Deprecated
    public WebGlProgramEmulator(RenderMode renderMode, VertexShader vertexShader, List<Double> doubles, Paint paint) {
        this.renderMode = renderMode;
        if (doubles == null || doubles.isEmpty()) {
            throw new IllegalArgumentException("Buffer is empty");
        }
        if (doubles.size() % renderMode.getDoubleCount() != 0) {
            throw new IllegalArgumentException("Buffer has wrong size: " + doubles.size());
        }
        if (vertexShader == null) {
            throw new IllegalStateException("Vertex has not been set");
        }
        this.vertexShader = vertexShader;
        this.doubles = doubles;
        this.paint = paint;
    }


    public WebGlProgramEmulator setVertexShader(VertexShader vertexShader) {
        this.vertexShader = vertexShader;
        return this;
    }

    public WebGlProgramEmulator setDoubles(List<Double> doubles) {
        this.doubles = doubles;
        return this;
    }

    public WebGlProgramEmulator setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        return this;
    }

    public WebGlProgramEmulator setPaint(Paint paint) {
        this.paint = paint;
        return this;
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

    public RenderMode getRenderMode() {
        return renderMode;
    }
}
