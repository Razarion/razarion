package com.btxtech.renderer.emulation.webgl;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.shared.primitives.Vertex4;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class WebGlEmulator {
    private List<Double> doubles = new ArrayList<>();
    private Double minDepth;
    private Double maxDepth;
    private Canvas canvas;
    private VertexShader vertexShader;

    public void init(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setVertexShader(VertexShader vertexShader) {
        this.vertexShader = vertexShader;
    }

    public void fillBuffer(List<Double> doubles) {
        this.doubles.addAll(doubles);
    }

    public void drawArrays() {
        if (doubles == null || doubles.isEmpty()) {
            System.out.println("Nothing to draw");
        }
        if (doubles.size() % 9 != 0) {
            throw new IllegalArgumentException("List must be a multiple of 9 because there are 3 corners with x,y,z in a triangle: " + doubles.size());
        }

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        minDepth = null;
        maxDepth = null;

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

        // TODO projectionTransformation.setAspectRatio(canvas.getWidth() / canvas.getHeight());

        for (int i = 0; i < doubles.size() / 9; i++) {
            int base = i * 9;
            Vertex vertexA = new Vertex(doubles.get(base), doubles.get(base + 1), doubles.get(base + 2));
            Vertex vertexB = new Vertex(doubles.get(base + 3), doubles.get(base + 4), doubles.get(base + 5));
            Vertex vertexC = new Vertex(doubles.get(base + 6), doubles.get(base + 7), doubles.get(base + 8));

            drawTriangle(gc, vertexA, vertexB, vertexC);
        }
        System.out.println("--------------------------------------------");
        System.out.println("minDepth: " + minDepth);
        System.out.println("maxDepth: " + maxDepth);
        if (maxDepth != null && minDepth != null) {
            System.out.println("delta: " + (maxDepth - minDepth));
            System.out.println("factor: " + 2.0 / (maxDepth - minDepth));
        }
        System.out.println("--------------------------------------------");

        gc.restore();
    }

    private void drawTriangle(GraphicsContext gc, Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        Vertex4 clipA = toClip(vertexA);
        Vertex4 clipB = toClip(vertexB);
        Vertex4 clipC = toClip(vertexC);

        DecimalPosition ndcA = toNdcVertex(clipA);
        DecimalPosition ndcB = toNdcVertex(clipB);
        DecimalPosition ndcC = toNdcVertex(clipC);

        gc.setStroke(Color.BLUE);
        // Polygon and translate does not work in JavaFX 2.2
        // http://stackoverflow.com/questions/13236523/unexpected-behaviour-of-javafx-graphicscontext-translate
        // http://javafx-jira.kenai.com/browse/RT-26119
        gc.strokeLine(ndcA.getX(), ndcA.getY(), ndcB.getX(), ndcB.getY());
        gc.strokeLine(ndcB.getX(), ndcB.getY(), ndcC.getX(), ndcC.getY());
        gc.strokeLine(ndcC.getX(), ndcC.getY(), ndcA.getX(), ndcA.getY());
        //gc.strokeText("X", ndcA.getX(), ndcA.getY(), 0.001);
    }

    private DecimalPosition toNdcVertex(Vertex4 vertex4) {
        DecimalPosition normalizedDeviceCoordinates = new DecimalPosition(vertex4.getX() / vertex4.getW(), vertex4.getY() / vertex4.getW());

        double depth = vertex4.getZ() / vertex4.getW();
        if (minDepth == null) {
            minDepth = depth;
        } else {
            minDepth = Math.min(minDepth, depth);
        }
        if (maxDepth == null) {
            maxDepth = depth;
        } else {
            maxDepth = Math.max(maxDepth, depth);
        }

        return normalizedDeviceCoordinates;
    }

    private Vertex4 toClip(Vertex vertex) {
        if (vertexShader == null) {
            throw new IllegalStateException("Vertex has not been set");
        }
        return vertexShader.process(vertex);
    }

    public double getAspectRatio() {
        return canvas.getWidth() / canvas.getHeight();
    }
}
