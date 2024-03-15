package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.gui.scenarioplayback.ScenarioPlaybackController;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.DiffTriangleElement;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.mocks.TestFloat32Array;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Created by Beat
 * on 30.06.2017.
 */
@Singleton
public class WeldTestRenderer {
    private static final double LINE_WIDTH = 0.1;
    public static final double FAT_LINE_WIDTH = 0.3;
    private static final int GRID_SPACING_100 = 100;
    private static final int GRID_SPACING_08 = 8;
    private static final Color BASE_ITEM_TYPE_BG_COLOR_ACTIVE = new Color(0.8, 0, 0, 0.2);
    private static final Color BASE_ITEM_TYPE_BG_COLOR_PASSIVE = new Color(0.0, 0.8, 0.0, 0.2);
    private static final Color BASE_ITEM_TYPE_COLOR = new Color(0.5, 0.5, 1, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR = new Color(0, 0.3, 0, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR_HIGHLIGHTED = new Color(1, 1, 0, 1);
    private static final Color BASE_ITEM_TYPE_WEAPON_COLOR = new Color(1, 1, 0, 1);
    private static final Color BASE_ITEM_TYPE_HEADING_COLOR = new Color(1, 0.3, 0, 1);
    private static final Color RESOURCE_ITEM_TYPE_COLOR = new Color(0.8, 0.8, 0, 1);
    private static final Color BOX_ITEM_TYPE_COLOR = new Color(1, 0.0, 1, 1);
    private static final double SYNC_ITEM_DISPLAY_FRONT_ANGEL = MathHelper.gradToRad(60);
    @Inject
    private TerrainService terrainService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private SyncItemContainerServiceImpl syncItemContainerService;
    @Inject
    private ItemTypeService itemTypeService;
    private Canvas canvas;
    private GraphicsContext gc;
    private double scale;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;
    private TerrainShapeManager actual;
    private TerrainShapeTile[][] terrainShapeTiles;
    private UserDataRenderer userDataRenderer;
    private UserDataRenderer moveUserDataRenderer;
    private double zMin = -2;
    private double zMax = 0.5;
    private WeldTestController weldTestController;

    public void init(Canvas canvas, double scale) {
        this.canvas = canvas;
        this.scale = scale;
    }

    public GraphicsContext getGc() {
        return gc;
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
        drawGrid(gc, canvasWidth, canvasHeight, (int) (GRID_SPACING_08 * scale), Color.LIGHTGRAY);

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

    public void render(ScenarioPlaybackController scenarioPlaybackController) {
        preRender();

        doRender(scenarioPlaybackController);

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
                gc.fillOval(start.getX() - strokeWidth * 2.0, start.getY() - strokeWidth * 2.0, strokeWidth * 4.0, strokeWidth * 4.0);
            }
        }
    }

    public void fillPolygon(List<DecimalPosition> polygon, Color color) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));

        double[] xCorners = new double[polygon.size()];
        double[] yCorners = new double[polygon.size()];
        for(int i = 0; i<polygon.size();i++) {
            DecimalPosition position = polygon.get(i);
                    xCorners[i] = position.getX();
                    yCorners[i] = position.getY();
        }

        gc.fillPolygon(xCorners, yCorners, polygon.size());
    }

    public void fillRectangle(Rectangle2D rectangle, Color color) {
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.8));
        gc.fillRect(rectangle.getStart().getX(), rectangle.getStart().getY(), rectangle.width(), rectangle.height());
    }

    public void strokeVertexPolygon(List<Vertex> polygon, double strokeWidth, Color color, boolean showPoint) {
        strokePolygon(Vertex.toXY(polygon), strokeWidth, color, showPoint);
    }

    private void fillVertexPolygon(List<Vertex> polygon, Color color) {
        fillPolygon(Vertex.toXY(polygon), color);
    }

    public void strokeLine(List<DecimalPosition> line, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < line.size() - 1; i++) {
            DecimalPosition start = line.get(i);
            DecimalPosition end = line.get(i + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
                if (i == line.size() - 1) {
                    gc.fillOval(end.getX() - strokeWidth * 5.0, end.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
                }
            }
        }
    }

    public void setup(WeldTestController weldTestController, Object[] userObjects) {
        this.weldTestController = weldTestController;
        if (userObjects != null && userObjects.length > 0) {
            userDataRenderer = new UserDataRenderer(this, userObjects);
        }
        actual = (TerrainShapeManager) SimpleTestEnvironment.readField("terrainShape", terrainService);
        try {
            Field field = TerrainShapeManager.class.getDeclaredField("terrainShapeTiles");
            field.setAccessible(true);
            terrainShapeTiles = (TerrainShapeTile[][]) field.get(actual);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setMoveUserDataRenderer(Object[] userObjects) {
        if (userObjects != null) {
            moveUserDataRenderer = new UserDataRenderer(this, userObjects);
        } else {
            moveUserDataRenderer = null;
        }
    }

    public double getZMin() {
        return zMin;
    }

    public void setZMin(double zMin) {
        this.zMin = zMin;
    }

    public double getZMax() {
        return zMax;
    }

    public void setZMax(double zMax) {
        this.zMax = zMax;
    }

    protected void doRender(ScenarioPlaybackController scenarioPlaybackController) {
        Index fromTileIndex = new Index(0, 0);
        Index toTileIndex = fromTileIndex.add(2, 2);

        if (weldTestController.renderTerrainTileWater() || weldTestController.renderTerrainTileGround() || weldTestController.renderTerrainTileSlope() || weldTestController.renderTerrainTileHeight() || weldTestController.renderTerrainTileTerrainType() || weldTestController.renderTerrainTileTerrainObject()) {
            doRenderTile(fromTileIndex, toTileIndex);
        }

        if (weldTestController.renderShapeAccess()) {
            renderTerrainShapeAccess();
        }

        // renderTerrainPathingSurfaceAccess();
        if (weldTestController.renderShapeTerrainType() || weldTestController.renderShapeTerrainHeight() || weldTestController.renderShapeFractionalSlope() || weldTestController.renderShapeObstacles() || weldTestController.renderGroundSlopeConnections() || weldTestController.renderShapeWater() || weldTestController.renderShapeTerrainObject()) {
            doRenderShape();
        }
        if (weldTestController.renderSyncItems()) {
            renderSyncItems();
        }
        if (userDataRenderer != null) {
            userDataRenderer.render();
        }
        if (moveUserDataRenderer != null) {
            moveUserDataRenderer.render();
        }
        if (scenarioPlaybackController != null) {
            scenarioPlaybackController.render(this);
        }
        if (DebugHelperStatic.getPolygon() != null) {
            strokePolygon(DebugHelperStatic.getPolygon(), FAT_LINE_WIDTH, Color.BLUE, true);
        }
        if (DebugHelperStatic.getPositions() != null) {
            drawPositions(DebugHelperStatic.getPositions(), FAT_LINE_WIDTH, Color.RED);
        }
        if (weldTestController.renderPolygon()) {
            strokePolygon(weldTestController.getPolygon(), 1, Color.RED, true);
        }
        if (weldTestController.renderPositions()) {
            drawPositions(weldTestController.getPositions(), 1, Color.RED);
        }
    }

    private void renderTerrainShapeAccess() {
        DecimalPosition from = new DecimalPosition(0, 0);
        double length = 300;

        for (double x = from.getX(); x < from.getX() + length; x++) {
            for (double y = from.getY(); y < from.getY() + length; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                try {
                    double z = terrainService.getSurfaceAccess().getInterpolatedZ(samplePosition);
                    // Z
                    gc.setFill(color4Z(z));
                } catch (Exception e) {
                    gc.setFill(Color.RED);
                    e.printStackTrace();
                }
                gc.fillRect(x, y, 0.5, 0.5);
                // Norm
                Vertex norm = terrainService.getSurfaceAccess().getInterpolatedNorm(samplePosition);
                if (MathHelper.compareWithPrecision(norm.magnitude(), 1.0)) {
                    gc.setFill(color4Norm(norm));
                } else {
                    gc.setFill(Color.BLACK);
                }
                gc.fillRect(x + 0.5, y, 0.5, 0.5);
                // TerrainType
                TerrainType terrainType = terrainService.getPathingAccess().getTerrainType(samplePosition);
                gc.setFill(color4TerrainType(terrainType));
                gc.fillRect(x + 0.5, y + 0.5, 0.5, 0.5);
            }
        }
    }

    private void doRenderTile(Index from, Index to) {
        for (int tileX = from.getX(); tileX <= to.getX(); tileX++) {
            for (int tileY = from.getY(); tileY <= to.getY(); tileY++) {
                TerrainTile terrainTile = terrainService.generateTerrainTile(new Index(tileX, tileY));
                if (terrainTile != null) {
                    drawTerrainTile(terrainTile);
                }
            }
        }
    }

    public void drawTerrainTile(TerrainTile terrainTile) {
        if (weldTestController.renderTerrainTileWater()) {
            if (terrainTile.getTerrainWaterTiles() != null) {
                gc.setLineWidth(LINE_WIDTH);
                gc.setStroke(Color.BLUE);
//                terrainTile.getTerrainWaterTiles().forEach(terrainWaterTile -> {
//                    if (terrainWaterTile.getPositions() != null) {
//                        drawTriangles(terrainWaterTile.getPositions());
//                    }
//                    if (terrainWaterTile.getShallowPositions() != null) {
//                        drawTriangles(terrainWaterTile.getShallowPositions());
//                    }
//                });
            }
        }
        if (weldTestController.renderTerrainTileGround()) {
            gc.setLineWidth(LINE_WIDTH);
            if (terrainTile.getGroundTerrainTiles() != null) {
                Arrays.stream(terrainTile.getGroundTerrainTiles()).forEach(groundTerrainTile -> drawTriangles(groundTerrainTile.positions));
            }
        }

        if (weldTestController.renderTerrainTileTerrainType() || weldTestController.renderTerrainTileHeight()) {
            drawNodes(terrainTile.getTerrainNodes(), terrainTile.getIndex());
        }

        if (weldTestController.renderTerrainTileSlope()) {
            gc.setLineWidth(LINE_WIDTH);
            if (terrainTile.getTerrainSlopeTiles() != null) {
                for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                    drawTerrainSlopeTile(terrainSlopeTile);
                }
            }
        }

        if (weldTestController.renderTerrainTileTerrainObject()) {
            if (terrainTile.getTerrainTileObjectLists() != null) {
                gc.setFill(Color.BROWN);
                Arrays.stream(terrainTile.getTerrainTileObjectLists()).forEach(terrainTileObjectList -> {
                    if (terrainTileObjectList.getTerrainObjectModels() != null) {
                        TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.getTerrainObjectConfigId());
                        Arrays.stream(terrainTileObjectList.getTerrainObjectModels()).forEach(terrainObjectModel -> {
                            // TODO NativeVertexDto br = terrainObjectModel.model.multiplyVertex(NativeUtil.toNativeVertex(-terrainObjectConfig.getRadius(), -terrainObjectConfig.getRadius(), 0), 1.0);
                            // TODO NativeVertexDto tl = terrainObjectModel.model.multiplyVertex(NativeUtil.toNativeVertex(terrainObjectConfig.getRadius(), terrainObjectConfig.getRadius(), 0), 1.0);
                            // TODO Rectangle2D rect = Rectangle2D.generateRectangleFromAnyPoints(new DecimalPosition(br.x, br.y), new DecimalPosition(tl.x, tl.y)); // If rotated, it is may upside down
                            // TODO gc.fillOval(rect.startX(), rect.startY(), rect.width(), rect.height());
                        });
                    }
                });
            }
        }
    }

    private void drawTriangles(Float32ArrayEmu float32ArrayEmu) {
        TestFloat32Array float32Array = (TestFloat32Array) float32ArrayEmu;
        for (int index = 0; index < float32Array.getDoubles().length; index += 9) {
            strokeZTriangle(float32Array.getDoubles(), index, index + 3, index + 6);
        }
    }

    private void fillTriangle(Vertex a, Vertex b, Vertex c) {
        // TODO gc.setFill(color4Norm(new Vertex(fillVertices[fillIndex], fillVertices[fillIndex + 1], fillVertices[fillIndex + 2])));
        double[] xCorners = new double[]{a.getX(), b.getX(), c.getX()};
        double[] yCorners = new double[]{a.getY(), b.getY(), c.getY()};
        gc.fillPolygon(xCorners, yCorners, 3);
    }

    private void fillTriangle(double[] groundVertices, double[] groundNorms, int index1, int index2, int index3) {
        int fillIndex = index2;
        double[] fillVertices = groundNorms;
        gc.setFill(color4Norm(new Vertex(fillVertices[fillIndex], fillVertices[fillIndex + 1], fillVertices[fillIndex + 2])));
        double[] xCorners = new double[]{groundVertices[index1], groundVertices[index2], groundVertices[index3]};
        double[] yCorners = new double[]{groundVertices[index1 + 1], groundVertices[index2 + 1], groundVertices[index3 + 1]};
        gc.fillPolygon(xCorners, yCorners, 3);
    }

    private void fillTriangle(double[] groundVertices, int index1, int index2, int index3) {
        double[] xCorners = new double[]{groundVertices[index1], groundVertices[index2], groundVertices[index3]};
        double[] yCorners = new double[]{groundVertices[index1 + 1], groundVertices[index2 + 1], groundVertices[index3 + 1]};
        gc.fillPolygon(xCorners, yCorners, 3);
    }

    public void showDifference(DiffTriangleElement diffTriangleElement) {
        switch (diffTriangleElement.getDifference()) {
            case XY:
                gc.setLineWidth(LINE_WIDTH);
                gc.setStroke(Color.RED);
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case Z:
                gc.setFill(new Color(1.0, 0.5, 0.5, 0.5));
                fillTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case XYZ:
                gc.setFill(new Color(1.0, 0.5, 0.5, 0.5));
                fillTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                gc.setLineWidth(LINE_WIDTH);
                gc.setStroke(Color.RED);
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case MISSING:
                gc.setLineWidth(LINE_WIDTH);
                gc.setStroke(new Color(1.0, 0.8, 0.0, 1.0));
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case UNEXPECTED:
                gc.setLineWidth(LINE_WIDTH);
                gc.setStroke(new Color(0.8, 0, 0.8, 1.0));
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            default:
                throw new IllegalArgumentException("Unknown Difference: " + diffTriangleElement.getDifference());
        }
    }

    private void strokeTriangle(double[] vertices, int index1, int index2, int index3) {
        gc.strokeLine(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1]);
        gc.strokeLine(vertices[index2], vertices[index2 + 1], vertices[index3], vertices[index3 + 1]);
        gc.strokeLine(vertices[index3], vertices[index3 + 1], vertices[index1], vertices[index1 + 1]);
    }

    private void strokeZTriangle(Vertex a, Vertex b, Vertex c) {
        strokeGradientLine(a, b, color4Z(a.getZ()), color4Z(b.getZ()));
        strokeGradientLine(b, c, color4Z(b.getZ()), color4Z(c.getZ()));
        strokeGradientLine(c, a, color4Z(c.getZ()), color4Z(a.getZ()));
    }

    private void strokeZTriangle(double[] vertices, int index1, int index2, int index3) {
        strokeGradientLine(vertices, index1, index2, color4Z(vertices[index1 + 2]), color4Z(vertices[index2 + 2]));
        strokeGradientLine(vertices, index2, index3, color4Z(vertices[index2 + 2]), color4Z(vertices[index3 + 2]));
        strokeGradientLine(vertices, index3, index1, color4Z(vertices[index3 + 2]), color4Z(vertices[index1 + 2]));
    }

    private void strokeZLine(double[] vertices, int index1, int index2) {
        strokeGradientLine(vertices, index1, index2, color4Z(vertices[index1 + 2]), color4Z(vertices[index2 + 2]));
    }

    private void strokeGradientTriangle(double[] vertices, int index1, int index2, int index3, Color color1, Color color2, Color color3) {
        strokeGradientLine(vertices, index1, index2, color1, color2);
        strokeGradientLine(vertices, index2, index3, color2, color3);
        strokeGradientLine(vertices, index3, index1, color3, color1);
    }

    private void strokeGradientLine(double[] vertices, int index1, int index2, Color color1, Color color2) {
        gc.setStroke(new LinearGradient(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1], false, CycleMethod.NO_CYCLE, new Stop(0, color1), new Stop(1, color2)));
        gc.strokeLine(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1]);
    }

    private void strokeGradientLine(Vertex a, Vertex b, Color color1, Color color2) {
        gc.setStroke(new LinearGradient(a.getX(), a.getY(), b.getX(), b.getY(), false, CycleMethod.NO_CYCLE, new Stop(0, color1), new Stop(1, color2)));
        gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private Color color4Z(double z) {
        double value = InterpolationUtils.interpolate(0, 1, zMin, zMax, z);
        value = MathHelper.clamp(value, 0, 1);
        return Color.color(value, 0, value);
    }

    private void drawTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        drawSlopeGeometry(terrainSlopeTile.getOuterSlopeGeometry());
        drawSlopeGeometry(terrainSlopeTile.getCenterSlopeGeometry());
        drawSlopeGeometry(terrainSlopeTile.getInnerSlopeGeometry());


        gc.setLineWidth(LINE_WIDTH);
        // TODO for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
        // TODO    int vertexScalarIndex = vertexIndex * 3;
        // fillTriangle(terrainSlopeTile.getVertices(), terrainSlopeTile.getNorms(), terrainSlopeTile.getTangents(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);
        // TODO strokeZTriangle(terrainSlopeTile.getVertices(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);

//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//            gc.setStroke(Color.GRAY);
//            gc.strokePolygon(xCorners, yCorners, 3);
        // gc.setFill(Color.color(1, 0, 0, 0.3));
        // gc.fillPolygon(xCorners, yCorners, 3);
        // TODO       }
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
    }

    private void drawSlopeGeometry(SlopeGeometry slopeGeometry) {
        if (slopeGeometry == null) {
            return;
        }
        drawTriangles(slopeGeometry.getPositions());
    }


    private void drawNodes(TerrainNode[][] terrainNodes, Index index) {
        if (terrainNodes == null) {
            return;
        }
        for (int x = 0; x < TerrainUtil.TERRAIN_TILE_NODES_COUNT; x++) {
            for (int y = 0; y < TerrainUtil.TERRAIN_TILE_NODES_COUNT; y++) {
                TerrainNode terrainNode = terrainNodes[x][y];
                if (terrainNode != null) {
                    DecimalPosition absoluteNodePosition = TerrainUtil.toTileAbsolute(index).add(TerrainUtil.toNodeAbsolute(new Index(x, y)));
                    drawNode(terrainNode, absoluteNodePosition);
                }
            }
        }
    }

    private void drawNode(TerrainNode terrainNode, DecimalPosition absoluteNodePosition) {
        if (weldTestController.renderTerrainTileTerrainType()) {
            TerrainType terrainType = TerrainType.fromOrdinal(terrainNode.getTerrainType());
            if (terrainType != null) {
                gc.setFill(color4TerrainType(terrainType));
                gc.fillRect(absoluteNodePosition.getX(), absoluteNodePosition.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
            }
        }
        if (weldTestController.renderTerrainTileHeight()) {
            gc.setFill(color4Z(terrainNode.getHeight()));
            gc.fillRect(absoluteNodePosition.getX(), absoluteNodePosition.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
        drawSubNodes(terrainNode.getTerrainSubNodes(), absoluteNodePosition, 0);
    }

    private void drawSubNodes(TerrainSubNode[][] terrainSubNodes, DecimalPosition absolutePosition, int depth) {
        if (terrainSubNodes == null) {
            return;
        }
        double subNodeLength = TerrainUtil.calculateSubNodeLength(depth);
        for (int x = 0; x < terrainSubNodes.length; x++) {
            for (int y = 0; y < terrainSubNodes.length; y++) {
                TerrainSubNode terrainSubNode = terrainSubNodes[x][y];
                if (terrainSubNode != null) {
                    drawSubNode(terrainSubNode, absolutePosition.add(x * subNodeLength, y * subNodeLength), subNodeLength, depth);
                }
            }
        }
    }

    private void drawSubNode(TerrainSubNode terrainSubNode, DecimalPosition absolutePosition, double subNodeLength, int depth) {
        if (weldTestController.renderTerrainTileTerrainType()) {
            TerrainType terrainType = TerrainType.fromOrdinal(terrainSubNode.getTerrainType());
            if (terrainType != null) {
                gc.setFill(color4TerrainType(terrainType));
                gc.fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength - 0.1, subNodeLength - 0.1);
            }
        }
        if (weldTestController.renderTerrainTileHeight()) {
            if (terrainSubNode.getHeight() != null) {
                gc.setFill(color4Z(terrainSubNode.getHeight()));
                gc.fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength - 0.1, subNodeLength - 0.1);
            }
        }

//        gc.setStroke(new Color(0, 0, 1, 1));
//        gc.strokeRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//        if (terrainSubNode.getTerrainSubNodes() == null) {
//            if (terrainSubNode.isLand() == null || !terrainSubNode.isLand()) {
//                gc.setFill(new Color(1, 0, 0, 0.5));
//                gc.fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//            }
//
//            double height = terrainSubNode.getHeight();
//            double v = (height + 5) / 25.0;
//            gc.setFill(Color.color(v, v, v, 0.5));
//            gc.fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//        }
        drawSubNodes(terrainSubNode.getTerrainSubNodes(), absolutePosition, depth + 1);

    }

    private void doRenderShape() {
        for (int x = 0; x < actual.getTileXCount(); x++) {
            for (int y = 0; y < actual.getTileYCount(); y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    displayTerrainShapeTile(new Index(x, y), terrainShapeTile);
                }
            }
        }
    }

    private void renderSyncItems() {
        syncItemContainerService.iterateOverItems(false, true, null, syncItem -> {
            drawSyncItem(syncItem);
            return null;
        });
    }

//    private void displayClosedList() {
//        if (aStar == null) {
//            return;
//        }
//        Map<PathingNodeWrapper, AStarNode> closedList = (Map<PathingNodeWrapper, AStarNode>) SimpleTestEnvironment.readField("closedList", aStar);
//        for (Map.Entry<PathingNodeWrapper, AStarNode> entry : closedList.entrySet()) {
//            PathingNodeWrapper pathingNodeWrapper = entry.getKey();
//            if(pathingNodeWrapper.getNodeIndex() != null) {
//                Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(pathingNodeWrapper.getNodeIndex());
//                gc.setFill(new Color(0, 1, 1, 0.3));
//                gc.fillRect(rect.startX(), rect.startY(), rect.width() - 0.1, rect.height() - 0.1);
//            }
//            if(pathingNodeWrapper.getTerrainShapeSubNode() != null) {
//                double length = TerrainUtil.calculateSubNodeLength(pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//                gc.setFill(new Color(1, 0, 1, 0.3));
//                gc.fillRect(pathingNodeWrapper.getSubNodePosition().getX(), pathingNodeWrapper.getSubNodePosition().getY(), length - 0.1, length - 0.1);
//            }
//        }
//    }

    private void displayTerrainShapeTile(Index tileIndex, TerrainShapeTile terrainShapeTile) {
        gc.setLineWidth(LINE_WIDTH * 4.0);
        gc.setStroke(Color.DARKGREEN);
        DecimalPosition absolute = TerrainUtil.toTileAbsolute(tileIndex);
        gc.strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH);
        displayNodes(absolute, terrainShapeTile);

        if (weldTestController.renderShapeFractionalSlope()) {
            displayFractionalSlope(terrainShapeTile.getFractionalSlopes());
        }
        if (weldTestController.renderShapeTerrainObject()) {
            displayShapeTerrainObject(terrainShapeTile.getNativeTerrainShapeObjectLists());
        }
    }

    private void displayNodes(DecimalPosition absoluteTile, TerrainShapeTile terrainShapeTile) {
        if (!terrainShapeTile.hasNodes()) {
            return;
        }
        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null) {
                return;
            }
            displayNode(absoluteTile, nodeRelativeIndex, terrainShapeNode);
        });

    }

    private void displayNode(DecimalPosition absoluteTile, Index nodeRelativeIndex, TerrainShapeNode terrainShapeNode) {
        DecimalPosition absolute = TerrainUtil.toNodeAbsolute(nodeRelativeIndex).add(absoluteTile);
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        if (weldTestController.renderShapeObstacles()) {
            displayObstacles(terrainShapeNode);
        }
        if (weldTestController.renderGroundSlopeConnections()) {
            displayGroundSlopeConnections(terrainShapeNode.getGroundSlopeConnections());
        }
        if (weldTestController.renderShapeWater()) {
            displayShapeWater(terrainShapeNode.getWaterSegments());
        }
        if (weldTestController.renderShapeTerrainType() && terrainShapeNode.getTerrainType() != null) {
            gc.setFill(color4TerrainType(terrainShapeNode.getTerrainType()));
            gc.fillRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
        if (weldTestController.renderShapeTerrainHeight() && terrainShapeNode.getGameEngineHeightOrNull() != null) {
            gc.setFill(color4Z(terrainShapeNode.getGameEngineHeightOrNull()));
            gc.fillRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
        displaySubNodes(0, absolute, terrainShapeNode.getTerrainShapeSubNodes());
    }

    private void displayGroundSlopeConnections(Map<Integer, List<List<Vertex>>> groundSlopeConnectionList) {
        if (groundSlopeConnectionList == null) {
            return;
        }
        groundSlopeConnectionList.forEach((groundId, groundSlopeConnections) -> {
            Color color = Color.GREEN;
            if (groundId == null) {
                color = Color.DARKGREEN;
            } else if (groundId == 253) {
                color = Color.GRAY;
            }
            for (List<Vertex> groundSlopeConnection : groundSlopeConnections) {
                fillVertexPolygon(groundSlopeConnection, color);
                strokeVertexPolygon(groundSlopeConnection, LINE_WIDTH, color, true);
            }
        });
    }

    private void displayShapeWater(Map<Integer, List<List<Vertex>>> waterSegmentList) {
        if (waterSegmentList == null) {
            return;
        }
        waterSegmentList.forEach((groundId, waterSegments) -> {
            for (List<Vertex> waterSegment : waterSegments) {
                strokeVertexPolygon(waterSegment, LINE_WIDTH, Color.BLUE, true);
            }
        });
    }

    private void displayObstacles(TerrainShapeNode terrainShapeNode) {
        if (terrainShapeNode.getObstacles() == null) {
            return;
        }
        for (Obstacle obstacle : terrainShapeNode.getObstacles()) {
            if (obstacle instanceof ObstacleSlope) {
                ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
                gc.setStroke(new Color(1.0, 0.0, 0.0, 0.3));
                gc.strokeLine(obstacleSlope.getPoint1().getX(), obstacleSlope.getPoint1().getY(), obstacleSlope.getPoint2().getX(), obstacleSlope.getPoint2().getY());
                gc.setStroke(new Color(0.0, 1.0, 0.0, 0.3));
                gc.strokeLine(obstacleSlope.getPoint1().getX(), obstacleSlope.getPoint1().getY(), obstacleSlope.getPoint1().getX() + obstacleSlope.getPoint1Direction().getX(), obstacleSlope.getPoint1().getY() + obstacleSlope.getPoint1Direction().getY());
//                gc.setStroke(new Color(0.0, 1.0, 0.0, 0.3));
//                gc.strokeLine(obstacleSlope.getPoint2().getX(), obstacleSlope.getPoint2().getY(), obstacleSlope.getPoint2().getX() + obstacleSlope.getPoint2Direction().getX(), obstacleSlope.getPoint2().getY() + obstacleSlope.getPoint2Direction().getY());
            } else if (obstacle instanceof ObstacleTerrainObject) {
                ObstacleTerrainObject obstacleTerrainObject = (ObstacleTerrainObject) obstacle;
                gc.setStroke(Color.RED);
                gc.fillOval(obstacleTerrainObject.getCircle().getCenter().getX() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getCenter().getY() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius());
            } else {
                throw new IllegalArgumentException("Unknown: " + obstacle);
            }
        }
    }

    private void displaySubNodes(int depth, DecimalPosition absolute, TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return;
        }
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        TerrainShapeSubNode bottomLeft = terrainShapeSubNodes[0];
        if (bottomLeft != null) {
            displaySubNode(depth, absolute, bottomLeft);
        }
        TerrainShapeSubNode bottomRight = terrainShapeSubNodes[1];
        if (bottomRight != null) {
            displaySubNode(depth, absolute.add(subLength, 0), bottomRight);
        }
        TerrainShapeSubNode topRight = terrainShapeSubNodes[2];
        if (topRight != null) {
            displaySubNode(depth, absolute.add(subLength, subLength), topRight);
        }
        TerrainShapeSubNode topLeft = terrainShapeSubNodes[3];
        if (topLeft != null) {
            displaySubNode(depth, absolute.add(0, subLength), topLeft);
        }
    }

    private void displaySubNode(int depth, DecimalPosition absolute, TerrainShapeSubNode terrainShapeSubNode) {
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        gc.setStroke(Color.BLUEVIOLET);
        gc.setLineWidth(LINE_WIDTH);
        // gc.strokeRect(absolute.getX(), absolute.getY(), subLength, subLength);
        displaySubNodes(depth + 1, absolute, terrainShapeSubNode.getTerrainShapeSubNodes());
        if (weldTestController.renderShapeTerrainType() && terrainShapeSubNode.getTerrainType() != null) {
            gc.setFill(color4TerrainType(terrainShapeSubNode.getTerrainType()));
            gc.fillRect(absolute.getX(), absolute.getY(), subLength - 0.1, subLength - 0.1);
        }
        if (weldTestController.renderShapeTerrainHeight() && terrainShapeSubNode.getHeight() != null) {
            gc.setFill(color4Z(terrainShapeSubNode.getHeight()));
            gc.fillRect(absolute.getX(), absolute.getY(), subLength - 0.1, subLength - 0.1);
        }
    }

    private void displayFractionalSlope(List<FractionalSlope> fractionalSlopes) {
        if (fractionalSlopes == null) {
            return;
        }
        for (FractionalSlope fractionalSlope : fractionalSlopes) {
            List<DecimalPosition> inner = new ArrayList<>();
            List<DecimalPosition> outer = new ArrayList<>();
            for (FractionalSlopeSegment fractionalSlopeSegment : fractionalSlope.getFractionalSlopeSegments()) {
                inner.add(fractionalSlopeSegment.getInner());
                outer.add(fractionalSlopeSegment.getOuter());
            }
            strokeLine(inner, LINE_WIDTH, Color.PINK, true);
            strokeLine(outer, LINE_WIDTH, Color.AQUA, true);
        }
    }

    private void displayShapeTerrainObject(NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists) {
        if (nativeTerrainShapeObjectLists == null) {
            return;
        }
        gc.setStroke(Color.BROWN);
        gc.setLineWidth(FAT_LINE_WIDTH);
        Arrays.stream(nativeTerrainShapeObjectLists).forEach(nativeTerrainShapeObjectList -> {
            if (nativeTerrainShapeObjectList.terrainShapeObjectPositions != null) {
                double radius = terrainTypeService.getTerrainObjectConfig(nativeTerrainShapeObjectList.terrainObjectConfigId).getRadius();
                Arrays.stream(nativeTerrainShapeObjectList.terrainShapeObjectPositions).forEach(nativeTerrainShapeObjectPosition -> {
                    // TODO double correctedRadius = radius * nativeTerrainShapeObjectPosition.scale;
                    // TODO gc.strokeOval(nativeTerrainShapeObjectPosition.x - correctedRadius, nativeTerrainShapeObjectPosition.y - correctedRadius, 2.0 * correctedRadius, 2.0 * correctedRadius);
                });
            }
        });
    }


    public void drawSyncBaseItemInfo(SyncBaseItemInfo syncBaseItemInfo, boolean highlight) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
        DecimalPosition position = syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition();
        if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
            if (syncBaseItemInfo.getSyncPhysicalAreaInfo().getWayPositions() != null || syncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity() != null) {
                gc.setFill(BASE_ITEM_TYPE_BG_COLOR_ACTIVE);
            } else {
                gc.setFill(BASE_ITEM_TYPE_BG_COLOR_PASSIVE);
            }
            gc.fillOval(position.getX() - baseItemType.getPhysicalAreaConfig().getRadius(), position.getY() - baseItemType.getPhysicalAreaConfig().getRadius(), baseItemType.getPhysicalAreaConfig().getRadius() * 2, baseItemType.getPhysicalAreaConfig().getRadius() * 2);
            gc.setFill(BASE_ITEM_TYPE_COLOR);
            fillDirectionMarker(position, baseItemType.getPhysicalAreaConfig().getRadius(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle());
            gc.setStroke(highlight ? BASE_ITEM_TYPE_LINE_COLOR_HIGHLIGHTED : BASE_ITEM_TYPE_LINE_COLOR);
            gc.setLineWidth(0.1);
            strokeDirectionMarker(position, baseItemType.getPhysicalAreaConfig().getRadius(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle());
            gc.setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
            gc.setLineWidth(0.5);
            createHeadingLine(position, baseItemType.getPhysicalAreaConfig().getRadius(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle());
            if (syncBaseItemInfo.getSyncPhysicalAreaInfo().getWayPositions() != null) {
                strokeCurveDecimalPosition(syncBaseItemInfo.getSyncPhysicalAreaInfo().getWayPositions(), 0.1, Color.CADETBLUE, true);
            }
        } else {
            gc.setFill(BASE_ITEM_TYPE_COLOR);
            gc.fillOval(position.getX() - baseItemType.getPhysicalAreaConfig().getRadius(), position.getY() - baseItemType.getPhysicalAreaConfig().getRadius(), baseItemType.getPhysicalAreaConfig().getRadius() * 2, baseItemType.getPhysicalAreaConfig().getRadius() * 2);
        }
    }

    public void drawSyncItem(SyncItem syncItem) {
        SyncPhysicalArea syncPhysicalArea = syncItem.getSyncPhysicalArea();
        if (!syncPhysicalArea.hasPosition()) {
            return;
        }
        DecimalPosition position = syncPhysicalArea.getPosition2d();
        if (syncItem instanceof SyncBaseItem) {
            gc.setFill(BASE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncResourceItem) {
            gc.setFill(RESOURCE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncBoxItem) {
            gc.setFill(BOX_ITEM_TYPE_COLOR);
        } else {
            throw new IllegalArgumentException("Unknown SyncItem: " + syncItem);
        }
        if (syncItem.getSyncPhysicalArea().canMove()) {
            fillDirectionMarker(syncItem.getSyncPhysicalArea().getPosition2d(), syncItem.getSyncPhysicalArea().getRadius(), syncItem.getSyncPhysicalArea().getAngle());
            gc.setStroke(BASE_ITEM_TYPE_LINE_COLOR);
            gc.setLineWidth(0.1);
            strokeDirectionMarker(syncItem.getSyncPhysicalArea().getPosition2d(), syncItem.getSyncPhysicalArea().getRadius(), syncItem.getSyncPhysicalArea().getAngle());
            gc.setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
            gc.setLineWidth(0.5);
            createHeadingLine(syncItem.getSyncPhysicalArea().getPosition2d(), syncItem.getSyncPhysicalArea().getRadius(), syncItem.getSyncPhysicalArea().getAngle());
        } else {
            gc.fillOval(position.getX() - syncPhysicalArea.getRadius(), position.getY() - syncPhysicalArea.getRadius(), syncPhysicalArea.getRadius() * 2, syncPhysicalArea.getRadius() * 2);
        }

        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (syncBaseItem.getSyncWeapon() != null) {
                Matrix4 matrix4 = syncBaseItem.getSyncWeapon().createTurretMatrix();
                DecimalPosition canonStart = matrix4.multiply(Vertex.ZERO, 1.0).toXY();
                DecimalPosition canonEnd = matrix4.multiply(syncBaseItem.getSyncWeapon().getWeaponType().getTurretType().getMuzzlePosition(), 1.0).toXY();
                gc.setStroke(BASE_ITEM_TYPE_WEAPON_COLOR);
                gc.setLineWidth(0.5);
                gc.strokeLine(canonStart.getX(), canonStart.getY(), canonEnd.getX(), canonEnd.getY());
            }
            if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                Path path = syncBaseItem.getSyncPhysicalMovable().getPath();
                if (path != null && path.getCurrentWayPoint() != null) {
                    strokeCurveDecimalPosition(path.getWayPositions(), 0.1, Color.CADETBLUE, true);
                    gc.setStroke(Color.BLUEVIOLET);
                    gc.setLineWidth(0.5);
                    gc.strokeLine(syncBaseItem.getSyncPhysicalArea().getPosition2d().getX(), syncBaseItem.getSyncPhysicalArea().getPosition2d().getY(), path.getCurrentWayPoint().getX(), path.getCurrentWayPoint().getY());
                }
            }
        }
    }

    private void fillDirectionMarker(DecimalPosition position, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = position.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = position.getPointWithDistance(angel2, radius);
        DecimalPosition point3 = position.getPointWithDistance(angel3, radius);
        DecimalPosition point4 = position.getPointWithDistance(angel4, radius);

        gc.fillPolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void strokeDirectionMarker(DecimalPosition position, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = position.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = position.getPointWithDistance(angel2, radius);
        DecimalPosition point3 = position.getPointWithDistance(angel3, radius);
        DecimalPosition point4 = position.getPointWithDistance(angel4, radius);

        gc.strokePolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void createHeadingLine(DecimalPosition middle, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;

        DecimalPosition point1 = middle.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = middle.getPointWithDistance(angel2, radius);

        gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public void strokeCurveDecimalPosition(List<DecimalPosition> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            DecimalPosition start = curve.get(i);
            if (i + 1 < curve.size()) {
                DecimalPosition end = curve.get(i + 1);
                gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            }
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    public void strokeCircle(Circle2D circle2D, double lineWidth, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        gc.strokeOval(circle2D.getCenter().getX() - circle2D.getRadius(), circle2D.getCenter().getY() - circle2D.getRadius(), 2.0 * circle2D.getRadius(), 2.0 * circle2D.getRadius());
    }

    public void fillCircle(Circle2D circle2D, Color color) {
        gc.setFill(color);
        gc.fillOval(circle2D.getCenter().getX() - circle2D.getRadius(), circle2D.getCenter().getY() - circle2D.getRadius(), 2.0 * circle2D.getRadius(), 2.0 * circle2D.getRadius());
    }

    public void drawPosition(DecimalPosition position, double radius, Color color) {
        gc.setFill(color);
        gc.fillOval(position.getX() - radius, position.getY() - radius, radius * 2.0, radius * 2.0);
    }

    public void drawPositions(Collection<DecimalPosition> positions, double radius, Color color) {
        for (DecimalPosition position : positions) {
            drawPosition(position, radius, color);
        }
    }

    public static Color color4TerrainType(TerrainType terrainType) {
        if (terrainType == null) {
            terrainType = TerrainType.getNullTerrainType();
        }
        switch (terrainType) {
            case LAND:
                return Color.GREEN;
            case WATER:
                return Color.BLUE;
            case LAND_COAST:
                return Color.LIGHTGREEN;
            case WATER_COAST:
                return Color.SANDYBROWN;
            case BLOCKED:
                return Color.RED;
            default:
                throw new IllegalArgumentException("Unknown terrainType: " + terrainType);
        }
    }

    public static Color color4Norm(Vertex norm) {
        Vertex colorNorm = norm.multiply(0.5).add(0.5, 0.5, 0.5);
        return Color.color(colorNorm.getX(), colorNorm.getY(), colorNorm.getZ());
    }
}
