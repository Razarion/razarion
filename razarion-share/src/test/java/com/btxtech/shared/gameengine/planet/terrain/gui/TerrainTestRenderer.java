package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 25.06.2016.
 */
public class TerrainTestRenderer {
    private static  List<DecimalPosition> driveway1 = Arrays.asList(new DecimalPosition(151.5791764537405, 323.51025766525436), new DecimalPosition(150.40130510688527, 317.34276071913376), new DecimalPosition(150.6945631890857, 311.104609128963), new DecimalPosition(152.4164072176388, 305.17565539631994), new DecimalPosition(153.57064513635234, 303.07390726443987), new DecimalPosition(173.0173636420843, 307.7456456302757), new DecimalPosition(172.11217078445617, 308.6508384879038), new DecimalPosition(170.69177556525412, 311.4385210670791), new DecimalPosition(170.20234072820566, 314.52869101082854), new DecimalPosition(170.69177556525412, 317.618860954578));
    private static List<DecimalPosition> driveway2 = Arrays.asList(new DecimalPosition(229.7228102503087, 93.74624780631167), new DecimalPosition(224.44156390181416, 91.23561877923918), new DecimalPosition(219.0301365178866, 90.70502016604792), new DecimalPosition(214.30867856773557, 88.31039033373416), new DecimalPosition(212.37667655561458, 86.47483517864345), new DecimalPosition(207.31656908016808, 85.89967758800603), new DecimalPosition(204.68721285618236, 84.5025556830088), new DecimalPosition(201.95858479347882, 84.16598884066639), new DecimalPosition(199.40632569408996, 82.78004554683035), new DecimalPosition(195.25122088488845, 82.22631874915169), new DecimalPosition(190.84172066846975, 80.18522262084939), new DecimalPosition(185.8680081341589, 79.44441571008328), new DecimalPosition(183.11608334016802, 77.86214096391089), new DecimalPosition(186.25459086571033, 58.10993170742229), new DecimalPosition(189.54143212205656, 59.78466097597894), new DecimalPosition(195.41275501518055, 60.71458716637102), new DecimalPosition(200.67024363412307, 62.9744566978259), new DecimalPosition(205.55178332828845, 63.7476166302083), new DecimalPosition(208.60088079313638, 65.30120938653725), new DecimalPosition(211.79012624775928, 65.80633624292527), new DecimalPosition(214.9227553360673, 67.40249048724473), new DecimalPosition(220.79407822919129, 68.33241667763681), new DecimalPosition(223.14086157692904, 70.36623966305281), new DecimalPosition(228.71622673527955, 73.20703010145694), new DecimalPosition(234.8965666227785, 74.18589977555386), new DecimalPosition(241.04158959742364, 77.25730502360652));
    private static final double LINE_WIDTH = 0.2;
    private static final int GRID_SPACING_100 = 100;
    private static final int GRID_SPACING_20 = 20;
    private final Collection<TerrainTile> expectedTiles;
    private final Collection<TerrainTile> actualTiles;
    private TriangleContainer triangleContainer;
    private Canvas canvas;
    private GraphicsContext gc;
    private double scale;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;

    public TerrainTestRenderer(Collection<TerrainTile> expectedTiles, Collection<TerrainTile> actualTiles, TriangleContainer triangleContainer) {
        this.expectedTiles = expectedTiles;
        this.actualTiles = actualTiles;
        this.triangleContainer = triangleContainer;
    }

    public void init(Canvas canvas, double scale) {
        this.canvas = canvas;
        this.scale = scale;
    }

    public DecimalPosition convertMouseToModel(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        return decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale).sub(shift);
    }

    public boolean shifting(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        DecimalPosition position = decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale);

        boolean isShifted = false;
        if (lastShiftPosition != null) {
            DecimalPosition delta = position.sub(lastShiftPosition);
            if (!delta.equalsDeltaZero()) {
                shift = shift.add(delta);
                isShifted = true;
            }
        }
        lastShiftPosition = position;
        return isShifted;
    }

    public void stopShift() {
        lastShiftPosition = null;
    }

    protected void preRender() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc = canvas.getGraphicsContext2D();

        gc.translate(0, 0);
        gc.scale(1.0, 1.0);
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();

        // draw grid
        drawGrid(gc, canvasWidth, canvasHeight);

        gc.translate(canvasWidth / 2.0, canvasHeight / 2.0);
        gc.scale(scale, -scale);
        gc.translate(shift.getX(), shift.getY());
    }

    protected void postRender() {
        gc.restore();
        gc = null;
    }

    private void drawGrid(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        drawGrid(gc, canvasWidth, canvasHeight, (int) (GRID_SPACING_100 * scale), Color.GRAY);
        drawGrid(gc, canvasWidth, canvasHeight, (int) (GRID_SPACING_20 * scale), Color.LIGHTGRAY);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(shift.getX() * scale + canvasWidth / 2.0, 0, shift.getX() * scale + canvasWidth / 2.0, canvasHeight);
        gc.strokeLine(0, canvasHeight / 2.0 - shift.getY() * scale, canvasWidth, canvasHeight / 2.0 - shift.getY() * scale);
    }

    private void drawGrid(GraphicsContext gc, double canvasWidth, double canvasHeight, int gridSpacing, Paint color) {
        gc.setLineWidth(1);
        gc.setStroke(color);

        int verticalGrid = (int) Math.ceil(canvasWidth / gridSpacing) * gridSpacing;
        int verticalOffset = (int) (shift.getX() * scale + canvasWidth / 2.0) % gridSpacing;
        for (int x = 0; x <= verticalGrid; x += gridSpacing) {
            gc.strokeLine(x + verticalOffset, 0, x + verticalOffset, canvasHeight);
        }
        int horizontalGrid = (int) Math.ceil(canvasHeight / gridSpacing) * gridSpacing;
        int horizontalOffset = (int) (canvasHeight / 2.0 - shift.getY() * scale) % gridSpacing;
        for (int y = 0; y <= horizontalGrid; y += gridSpacing) {
            gc.strokeLine(0, y + horizontalOffset, canvasWidth, y + horizontalOffset);
        }
    }


    public void setZoom(double zoom) {
        if (zoom > 1.0) {
            scale = zoom;
        } else if (zoom < -1.0) {
            scale = -1.0 / zoom;
        } else {
            scale = 1.0;
        }
    }

    public double getZoom() {
        if (scale > 1.0) {
            return scale;
        } else if (scale < 1.0) {
            return -1.0 / scale;
        } else {
            return 1.0;
        }
    }

    public double getScale() {
        return scale;
    }

    public void render() {
        preRender();

        for (TerrainTile actual : actualTiles) {
            drawTerrainTile(actual);
        }
//        for (TerrainTile expected : expectedTiles) {
//            drawTerrainTile(expected);
//        }

//        drawTriangleContainer();

        strokePolygon(driveway1, 0.1, Color.PINK, true);
        strokePolygon(driveway2, 0.1, Color.PINK, true);

        postRender();
    }

    public void strokePolygon(List<DecimalPosition> polygon, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition start = polygon.get(i);
            DecimalPosition end = polygon.get(i + 1 < polygon.size() ? i + 1 : i - polygon.size() + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    public void drawTerrainTile(TerrainTile terrainTile) {
        gc.setLineWidth(LINE_WIDTH);
        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex], terrainTile.getGroundVertices()[vertexScalarIndex + 3], terrainTile.getGroundVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex + 1], terrainTile.getGroundVertices()[vertexScalarIndex + 4], terrainTile.getGroundVertices()[vertexScalarIndex + 7]};
            gc.setStroke(Color.LIGHTGREEN);
            gc.strokePolygon(xCorners, yCorners, 3);
        }

        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                drawTerrainSlopeTile(terrainSlopeTile);
            }
        }

        if (terrainTile.getTerrainWaterTile() != null) {
            drawTerrainWaterTile(terrainTile.getTerrainWaterTile());
        }

        drawDisplayHeight(terrainTile.getDisplayHeights(), terrainTile.getIndexX(), terrainTile.getIndexY());
    }

    private void drawTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        gc.setLineWidth(LINE_WIDTH);
        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
            gc.setStroke(Color.GRAY);
            gc.strokePolygon(xCorners, yCorners, 3);
            // gc.setFill(Color.color(1, 0, 0, 0.3));
            // gc.fillPolygon(xCorners, yCorners, 3);
        }
//        // Norm
//        gc.setStroke(Color.RED);
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
//            gc.strokeLine(xCorners[0], yCorners[0], xCorners[0] + normX0, yCorners[0] + normY0);
//            gc.strokeLine(xCorners[1], yCorners[1], xCorners[1] + normX1, yCorners[1] + normY1);
//            gc.strokeLine(xCorners[2], yCorners[2], xCorners[2] + normX2, yCorners[2] + normY2);
//        }
//        // Tangent
//        gc.setStroke(Color.BLUE);
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
//            gc.strokeLine(xCorners[0], yCorners[0], xCorners[0] + tangentX0, yCorners[0] + tangentY0);
//            gc.strokeLine(xCorners[1], yCorners[1], xCorners[1] + tangentX1, yCorners[1] + tangentY1);
//            gc.strokeLine(xCorners[2], yCorners[2], xCorners[2] + tangentX2, yCorners[2] + tangentY2);
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
//            gc.setFill(Color.color(slopeFactor, 0, 0, 0.1));
//
//            double radius = 1;
//            gc.fillOval(xCorner - radius, yCorner - radius, radius * 2.0, radius * 2.0);
//        }
//        // Splattings
//        gc.setStroke(Color.GREEN);
//        gc.setLineWidth(0.3);
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
//            gc.strokeLine(position.getX(), position.getY(), splattingAsPosition.getX(), splattingAsPosition.getY());
//        }

    }

    private void drawTerrainWaterTile(TerrainWaterTile terrainWaterTile) {
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(Color.BLUE);
        for (int vertexIndex = 0; vertexIndex < terrainWaterTile.getVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex], terrainWaterTile.getVertices()[vertexScalarIndex + 3], terrainWaterTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex + 1], terrainWaterTile.getVertices()[vertexScalarIndex + 4], terrainWaterTile.getVertices()[vertexScalarIndex + 7]};
            gc.strokePolygon(xCorners, yCorners, 3);
            // gc.setFill(Color.color(1, 0, 0, 0.3));
            // gc.fillPolygon(xCorners, yCorners, 3);
        }
    }

    private void drawDisplayHeight(double[] displayHeights, int tileX, int tileY) {
        gc.setLineWidth(LINE_WIDTH * 2);
        gc.setStroke(Color.RED);

        Index offset = new Index(TerrainUtil.toNodeIndex(tileX), TerrainUtil.toNodeIndex(tileY));

        for (int i = 0; i < displayHeights.length; i++) {
            double height = displayHeights[i] * 0.1;
            Index nodeIndex = TerrainUtil.arrayToFiledNodeIndex(i).add(offset);
            DecimalPosition position = TerrainUtil.toNodeAbsolute(nodeIndex);
            gc.strokeLine(position.getX(), position.getY(), position.getX(), position.getY() + height);

        }
    }

    private void drawTriangleContainer() {
        for (TriangleElement missing : triangleContainer.getMissingInExpected()) {
            double[] xCorners = new double[]{missing.getVertexA().getX(), missing.getVertexB().getX(), missing.getVertexC().getX()};
            double[] yCorners = new double[]{missing.getVertexA().getY(), missing.getVertexB().getY(), missing.getVertexC().getY()};
            gc.setStroke(Color.RED);
            gc.strokePolygon(xCorners, yCorners, 3);
        }
        for (TriangleElement missing : triangleContainer.getNonexistentInExpected()) {
            double[] xCorners = new double[]{missing.getVertexA().getX(), missing.getVertexB().getX(), missing.getVertexC().getX()};
            double[] yCorners = new double[]{missing.getVertexA().getY(), missing.getVertexB().getY(), missing.getVertexC().getY()};
            gc.setStroke(Color.YELLOWGREEN);
            gc.strokePolygon(xCorners, yCorners, 3);
        }
    }


}
