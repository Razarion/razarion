package com.btxtech.webglemulator.webgl;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 31.05.2016.
 */
public class AbstractWebGlEmulator {
    private List<WebGlProgramEmulator> webGlProgramEmulators = new ArrayList<>();
    private Canvas canvas;

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void fillBufferAndShader(RenderMode renderMode, VertexShader vertexShader, List<Double> doubles, Paint paint) {
        webGlProgramEmulators.add(new WebGlProgramEmulator(renderMode, vertexShader, doubles, paint));
    }

    public void drawArrays() {
        if(canvas == null) {
            return;
        }
        if (webGlProgramEmulators.isEmpty()) {
            System.out.println("Nothing to draw");
        }
        beforeDrawArrays();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();
        // Normalized device coordinates
        // E.g.: left=-1, right=1, top=1, bottom=-1, center=0
        double xScale = canvasWidth / 2.0;
        double xOffset = canvasWidth / 2.0;
        double yScale = -canvasHeight / 2.0;
        double yOffset = canvasHeight / 2.0;

        gc.translate(xOffset, yOffset);
        gc.scale(xScale, yScale);
        gc.setLineWidth(1.0 / Math.min(Math.abs(xScale), Math.abs(yScale)));

        for (WebGlProgramEmulator webGlProgramEmulator : webGlProgramEmulators) {
            switch (webGlProgramEmulator.getRenderMode()) {
                case TRIANGLES:
                    drawTriangles(gc, webGlProgramEmulator);
                    break;
                case LINES:
                    drawLines(gc, webGlProgramEmulator);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown render mode: " + webGlProgramEmulator.getRenderMode());
            }
        }
        gc.restore();

        afterDrawArrays();
    }

    private void drawLines(GraphicsContext gc, WebGlProgramEmulator webGlProgramEmulator) {
        for (int i = 0; i < webGlProgramEmulator.bufferSize() / 6; i++) {
            int base = i * 6;
            Vertex vertexA = new Vertex(webGlProgramEmulator.getBufferElement(base), webGlProgramEmulator.getBufferElement(base + 1), webGlProgramEmulator.getBufferElement(base + 2));
            Vertex vertexB = new Vertex(webGlProgramEmulator.getBufferElement(base + 3), webGlProgramEmulator.getBufferElement(base + 4), webGlProgramEmulator.getBufferElement(base + 5));

            drawLine(gc, webGlProgramEmulator.getVertexShader(), webGlProgramEmulator.getPaint(), vertexA, vertexB);
        }
    }

    private void drawTriangles(GraphicsContext gc, WebGlProgramEmulator webGlProgramEmulator) {
        for (int i = 0; i < webGlProgramEmulator.bufferSize() / 9; i++) {
            int base = i * 9;
            Vertex vertexA = new Vertex(webGlProgramEmulator.getBufferElement(base), webGlProgramEmulator.getBufferElement(base + 1), webGlProgramEmulator.getBufferElement(base + 2));
            Vertex vertexB = new Vertex(webGlProgramEmulator.getBufferElement(base + 3), webGlProgramEmulator.getBufferElement(base + 4), webGlProgramEmulator.getBufferElement(base + 5));
            Vertex vertexC = new Vertex(webGlProgramEmulator.getBufferElement(base + 6), webGlProgramEmulator.getBufferElement(base + 7), webGlProgramEmulator.getBufferElement(base + 8));

            drawTriangle(gc, webGlProgramEmulator.getVertexShader(), webGlProgramEmulator.getPaint(), vertexA, vertexB, vertexC);
        }
    }

    private void drawLine(GraphicsContext gc, VertexShader vertexShader, Paint paint, Vertex vertexA, Vertex vertexB) {
        Vertex4 clipA = vertexShader.process(vertexA);
        Vertex4 clipB = vertexShader.process(vertexB);

        Vertex ndcA = toNdcVertex(clipA);
        Vertex ndcB = toNdcVertex(clipB);

        if (ndcA == null || ndcB == null) {
            return;
        }

        gc.setStroke(paint);
        gc.strokeLine(ndcA.getX(), ndcA.getY(), ndcB.getX(), ndcB.getY());
    }

    private void drawTriangle(GraphicsContext gc, VertexShader vertexShader, Paint paint, Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        Vertex4 clipA = vertexShader.process(vertexA);
        Vertex4 clipB = vertexShader.process(vertexB);
        Vertex4 clipC = vertexShader.process(vertexC);

        Vertex ndcA = toNdcVertex(clipA);
        Vertex ndcB = toNdcVertex(clipB);
        Vertex ndcC = toNdcVertex(clipC);

        if (ndcA == null || ndcB == null || ndcC == null) {
            return;
        }

        vertexACallback(vertexA, ndcA);
        vertexBCallback(vertexB, ndcB);
        vertexCCallback(vertexC, ndcC);

        //double c = 0.5 * ndcA.getZ() + 0.5;
        // gc.setStroke(new Color(c, c, c, 1));
        gc.setStroke(paint);
        // Polygon and translate does not work in JavaFX 2.2
        // http://stackoverflow.com/questions/13236523/unexpected-behaviour-of-javafx-graphicscontext-translate
        // http://javafx-jira.kenai.com/browse/RT-26119
        gc.strokeLine(ndcA.getX(), ndcA.getY(), ndcB.getX(), ndcB.getY());
        gc.strokeLine(ndcB.getX(), ndcB.getY(), ndcC.getX(), ndcC.getY());
        gc.strokeLine(ndcC.getX(), ndcC.getY(), ndcA.getX(), ndcA.getY());
        //gc.strokeText("X", ndcA.getX(), ndcA.getY(), 0.001);
    }

    private Vertex toNdcVertex(Vertex4 vertex4) {
        double ndcX = vertex4.getX() / vertex4.getW();
        double ndcY = vertex4.getY() / vertex4.getW();
        double ndcZ = vertex4.getZ() / vertex4.getW();

        if (ndcX > 1 || ndcX < -1) {
            return null;
        }
        if (ndcY > 1 || ndcY < -1) {
            return null;
        }
        if (ndcZ > 1 || ndcZ < -1) {
            return null;
        }

        return new Vertex(ndcX, ndcY, ndcZ);
    }

    public double getAspectRatio() {
        return canvas.getWidth() / canvas.getHeight();
    }

    public DecimalPosition toClipCoordinates(DecimalPosition canvasPosition) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double xScale = canvasWidth / 2.0;
        double xOffset = canvasWidth / 2.0;
        double yScale = -canvasHeight / 2.0;
        double yOffset = canvasHeight / 2.0;

        return new DecimalPosition((canvasPosition.getX() - xOffset) / xScale, (canvasPosition.getY() - yOffset) / yScale);
    }

    protected void vertexACallback(Vertex vertex, Vertex ndc) {

    }

    protected void vertexBCallback(Vertex vertex, Vertex ndc) {

    }

    protected void vertexCCallback(Vertex vertex, Vertex ndc) {

    }

    protected void afterDrawArrays() {

    }

    protected void beforeDrawArrays() {

    }

}
