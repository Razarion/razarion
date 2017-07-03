package com.btxtech.shared.gameengine.planet.terrain.gui.teraintile;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * Created by Beat
 * 25.06.2016.
 */
public class TerrainTileTestRenderer extends AbstractTerrainTestRenderer {
    private final Collection<TerrainTile> expectedTiles;
    private final Collection<TerrainTile> actualTiles;
    private TriangleContainer triangleContainer;

    public TerrainTileTestRenderer(Collection<TerrainTile> expectedTiles, Collection<TerrainTile> actualTiles, TriangleContainer triangleContainer) {
        this.expectedTiles = expectedTiles;
        this.actualTiles = actualTiles;
        this.triangleContainer = triangleContainer;
    }

    @Override
    protected void doRender() {
        for (TerrainTile actual : actualTiles) {
            drawTerrainTile(actual);
        }
        if (expectedTiles != null) {
            for (TerrainTile expected : expectedTiles) {
                drawTerrainTile(expected);
            }
            drawTriangleContainer();
        }
    }

    
    public void drawTerrainTile(TerrainTile terrainTile) {
        getGc().setLineWidth(LINE_WIDTH);
        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex], terrainTile.getGroundVertices()[vertexScalarIndex + 3], terrainTile.getGroundVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex + 1], terrainTile.getGroundVertices()[vertexScalarIndex + 4], terrainTile.getGroundVertices()[vertexScalarIndex + 7]};
            getGc().setStroke(Color.LIGHTGREEN);
            getGc().strokePolygon(xCorners, yCorners, 3);
        }

        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                drawTerrainSlopeTile(terrainSlopeTile);
            }
        }

        if (terrainTile.getTerrainWaterTile() != null) {
            drawTerrainWaterTile(terrainTile.getTerrainWaterTile());
        }

        // drawDisplayHeight(terrainTile.getDisplayHeights(), terrainTile.getIndexX(), terrainTile.getIndexY());
    }

    private void drawTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        getGc().setLineWidth(LINE_WIDTH);
        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
            getGc().setStroke(Color.GRAY);
            getGc().strokePolygon(xCorners, yCorners, 3);
            // getGc().setFill(Color.color(1, 0, 0, 0.3));
            // getGc().fillPolygon(xCorners, yCorners, 3);
        }
//        // Norm
//        getGc().setStroke(Color.RED);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//
//            //  x, y of norm
//            final double AMPLIFIER = 2;
//            double normX0 = terrainSlopeTile.getNorms()[vertexScalarIndex] * AMPLIFIER;
//            double normY0 = terrainSlopeTile.getNorms()[vertexScalarIndex + 1] * AMPLIFIER;
//            double normX1 = terrainSlopeTile.getNorms()[vertexScalarIndex + 3] * AMPLIFIER;
//            double normY1 = terrainSlopeTile.getNorms()[vertexScalarIndex + 4] * AMPLIFIER;
//            double normX2 = terrainSlopeTile.getNorms()[vertexScalarIndex + 6] * AMPLIFIER;
//            double normY2 = terrainSlopeTile.getNorms()[vertexScalarIndex + 7] * AMPLIFIER;
//
//            getGc().strokeLine(xCorners[0], yCorners[0], xCorners[0] + normX0, yCorners[0] + normY0);
//            getGc().strokeLine(xCorners[1], yCorners[1], xCorners[1] + normX1, yCorners[1] + normY1);
//            getGc().strokeLine(xCorners[2], yCorners[2], xCorners[2] + normX2, yCorners[2] + normY2);
//        }
//        // Tangent
//        getGc().setStroke(Color.BLUE);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//
//            //  x, y of norm
//            final double AMPLIFIER = 2;
//            double tangentX0 = terrainSlopeTile.getTangents()[vertexScalarIndex] * AMPLIFIER;
//            double tangentY0 = terrainSlopeTile.getTangents()[vertexScalarIndex + 1] * AMPLIFIER;
//            double tangentX1 = terrainSlopeTile.getTangents()[vertexScalarIndex + 3] * AMPLIFIER;
//            double tangentY1 = terrainSlopeTile.getTangents()[vertexScalarIndex + 4] * AMPLIFIER;
//            double tangentX2 = terrainSlopeTile.getTangents()[vertexScalarIndex + 6] * AMPLIFIER;
//            double tangentY2 = terrainSlopeTile.getTangents()[vertexScalarIndex + 7] * AMPLIFIER;
//
//            getGc().strokeLine(xCorners[0], yCorners[0], xCorners[0] + tangentX0, yCorners[0] + tangentY0);
//            getGc().strokeLine(xCorners[1], yCorners[1], xCorners[1] + tangentX1, yCorners[1] + tangentY1);
//            getGc().strokeLine(xCorners[2], yCorners[2], xCorners[2] + tangentX2, yCorners[2] + tangentY2);
//
//        }
//        // SlopeFactor
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex++) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double xCorner = terrainSlopeTile.getVertices()[vertexScalarIndex];
//            double yCorner = terrainSlopeTile.getVertices()[vertexScalarIndex + 1];
//
//            double slopeFactor = terrainSlopeTile.getSlopeFactors()[vertexIndex];
//
//            getGc().setFill(Color.color(slopeFactor, 0, 0, 0.1));
//
//            double radius = 1;
//            getGc().fillOval(xCorner - radius, yCorner - radius, radius * 2.0, radius * 2.0);
//        }
//        // Splattings
//        getGc().setStroke(Color.GREEN);
//        getGc().setLineWidth(0.3);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex++) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double xCorner = terrainSlopeTile.getVertices()[vertexScalarIndex];
//            double yCorner = terrainSlopeTile.getVertices()[vertexScalarIndex + 1];
//
//            double splatting = terrainSlopeTile.getGroundSplattings()[vertexIndex];
//
//            DecimalPosition position = new DecimalPosition(xCorner, yCorner);
//            DecimalPosition splattingAsPosition = position.getPointWithDistance(MathHelper.QUARTER_RADIANT, splatting * 8);
//            getGc().strokeLine(position.getX(), position.getY(), splattingAsPosition.getX(), splattingAsPosition.getY());
//        }

    }

    private void drawTerrainWaterTile(TerrainWaterTile terrainWaterTile) {
        getGc().setLineWidth(LINE_WIDTH);
        getGc().setStroke(Color.BLUE);
        for (int vertexIndex = 0; vertexIndex < terrainWaterTile.getVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex], terrainWaterTile.getVertices()[vertexScalarIndex + 3], terrainWaterTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex + 1], terrainWaterTile.getVertices()[vertexScalarIndex + 4], terrainWaterTile.getVertices()[vertexScalarIndex + 7]};
            getGc().strokePolygon(xCorners, yCorners, 3);
            // getGc().setFill(Color.color(1, 0, 0, 0.3));
            // getGc().fillPolygon(xCorners, yCorners, 3);
        }
    }

    private void drawDisplayHeight(double[] displayHeights, int tileX, int tileY) {
        getGc().setLineWidth(LINE_WIDTH * 2);
        getGc().setStroke(Color.RED);

        Index offset = new Index(TerrainUtil.toNodeIndex(tileX), TerrainUtil.toNodeIndex(tileY));

        for (int i = 0; i < displayHeights.length; i++) {
            double height = displayHeights[i] * 0.1;
            Index nodeIndex = TerrainUtil.arrayToFiledNodeIndex(i).add(offset);
            DecimalPosition position = TerrainUtil.toNodeAbsolute(nodeIndex);
            getGc().strokeLine(position.getX(), position.getY(), position.getX(), position.getY() + height);

        }
    }

    private void drawTriangleContainer() {
        getGc().setLineWidth(0.1);
        for (TriangleElement missing : triangleContainer.getMissingInExpected()) {
            double[] xCorners = new double[]{missing.getVertexA().getX(), missing.getVertexB().getX(), missing.getVertexC().getX()};
            double[] yCorners = new double[]{missing.getVertexA().getY(), missing.getVertexB().getY(), missing.getVertexC().getY()};
            getGc().setStroke(Color.RED);
            getGc().strokePolygon(xCorners, yCorners, 3);
        }
        for (TriangleElement missing : triangleContainer.getNonexistentInExpected()) {
            double[] xCorners = new double[]{missing.getVertexA().getX(), missing.getVertexB().getX(), missing.getVertexC().getX()};
            double[] yCorners = new double[]{missing.getVertexA().getY(), missing.getVertexB().getY(), missing.getVertexC().getY()};
            getGc().setStroke(Color.YELLOWGREEN);
            getGc().strokePolygon(xCorners, yCorners, 3);
        }
    }


}
