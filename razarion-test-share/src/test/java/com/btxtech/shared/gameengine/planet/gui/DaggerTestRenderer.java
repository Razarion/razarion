package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.AStar;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.DiffTriangleElement;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.terrainPositionToNodeIndex;

/**
 * Canvas renderer driving the JavaFX test display. Renders terrain (LAND/WATER/BLOCKED
 * + grayscale heights), terrain objects, sync items with heading + path, the A* closed
 * list from the last pathing call, and a per-unit position trail. Also handles user-data
 * overlays (PositionMarker etc.) from {@link UserDataRenderer}.
 */
@Singleton
public class DaggerTestRenderer {
    public static final double LINE_WIDTH = 0.1;
    public static final double FAT_LINE_WIDTH = 0.3;
    private static final int GRID_SPACING_100 = 100;
    private static final int GRID_SPACING_08 = 8;
    private static final int TRAIL_MAX = 600;
    private static final Color BASE_ITEM_TYPE_BG_COLOR_ACTIVE = new Color(0.8, 0, 0, 0.2);
    private static final Color BASE_ITEM_TYPE_BG_COLOR_PASSIVE = new Color(0.0, 0.8, 0.0, 0.2);
    private static final Color BASE_ITEM_TYPE_COLOR = new Color(0.5, 0.5, 1, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR = new Color(0, 0.3, 0, 1);
    private static final Color BASE_ITEM_TYPE_HEADING_COLOR = new Color(1, 0.3, 0, 1);
    private static final Color RESOURCE_ITEM_TYPE_COLOR = new Color(0.8, 0.8, 0, 1);
    private static final Color BOX_ITEM_TYPE_COLOR = new Color(1, 0.0, 1, 1);
    private static final Color CLOSED_LIST_COLOR = new Color(0.0, 1.0, 1.0, 0.25);
    private static final Color CLOSED_LIST_START_COLOR = new Color(0.0, 1.0, 0.0, 0.6);
    private static final Color CLOSED_LIST_END_COLOR = new Color(1.0, 0.4, 0.0, 0.6);
    private static final Color TRAIL_COLOR = new Color(1.0, 1.0, 0.0, 0.7);
    private static final double SYNC_ITEM_DISPLAY_FRONT_ANGEL = MathHelper.gradToRad(60);

    private final TerrainService terrainService;
    private final TerrainTypeService terrainTypeService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final ItemTypeService itemTypeService;
    private final Map<Integer, Deque<DecimalPosition>> trailByItemId = new HashMap<>();

    private Canvas canvas;
    private GraphicsContext gc;
    private double scale;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;
    private TerrainShapeManager actual;
    private UserDataRenderer userDataRenderer;
    private UserDataRenderer moveUserDataRenderer;
    private double zMin = -2;
    private double zMax = 0.5;
    private TestController controller;

    @Inject
    public DaggerTestRenderer(ItemTypeService itemTypeService, SyncItemContainerServiceImpl syncItemContainerService, TerrainTypeService terrainTypeService, TerrainService terrainService) {
        this.itemTypeService = itemTypeService;
        this.syncItemContainerService = syncItemContainerService;
        this.terrainTypeService = terrainTypeService;
        this.terrainService = terrainService;
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
                return Color.GRAY;
            default:
                throw new IllegalArgumentException("Unknown terrainType: " + terrainType);
        }
    }

    public static Color color4Norm(Vertex norm) {
        Vertex colorNorm = norm.multiply(0.5).add(0.5, 0.5, 0.5);
        return Color.color(colorNorm.getX(), colorNorm.getY(), colorNorm.getZ());
    }

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

    private Rectangle2D viewportInModelSpace() {
        double halfWidthModel = canvas.getWidth() / (2.0 * scale);
        double halfHeightModel = canvas.getHeight() / (2.0 * scale);
        DecimalPosition center = shift.multiply(-1);
        return Rectangle2D.generateRectangleFromMiddlePoint(center, halfWidthModel * 2 + 4, halfHeightModel * 2 + 4);
    }

    private void preRender() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc = canvas.getGraphicsContext2D();

        gc.translate(0, 0);
        gc.scale(1.0, 1.0);
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();

        drawGrid(gc, canvasWidth, canvasHeight);

        gc.translate(canvasWidth / 2.0, canvasHeight / 2.0);
        gc.scale(scale, -scale);
        gc.translate(shift.getX(), shift.getY());
    }

    private void postRender() {
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
        if (gridSpacing < 4) {
            return;
        }
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

    public double getZoom() {
        if (scale > 1.0) {
            return scale;
        } else if (scale < 1.0) {
            return -1.0 / scale;
        }
        return 1.0;
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

    public double getScale() {
        return scale;
    }

    public void render() {
        preRender();
        try {
            doRender();
        } finally {
            postRender();
        }
    }

    private void doRender() {
        Rectangle2D viewport = viewportInModelSpace();

        if (controller.renderShapeTerrainType()) {
            renderShapeTerrainType(viewport);
        }
        if (controller.renderShapeTerrainHeight()) {
            renderShapeTerrainHeight(viewport);
        }
        if (controller.renderShapeTerrainObject()) {
            renderShapeTerrainObjects();
        }
        if (controller.renderAStarClosedList()) {
            renderAStarClosedList();
        }
        if (controller.renderTrail()) {
            renderTrail();
        }
        if (controller.renderSyncItems()) {
            renderSyncItems();
        }
        if (userDataRenderer != null) {
            userDataRenderer.render();
        }
        if (moveUserDataRenderer != null) {
            moveUserDataRenderer.render();
        }
        if (DebugHelperStatic.getPolygon() != null) {
            strokePolygon(DebugHelperStatic.getPolygon(), FAT_LINE_WIDTH, Color.BLUE, true);
        }
        if (DebugHelperStatic.getPositions() != null) {
            drawPositions(DebugHelperStatic.getPositions(), FAT_LINE_WIDTH, Color.RED);
        }
        if (controller.renderPolygon()) {
            strokePolygon(controller.getPolygon(), 1, Color.RED, true);
        }
        if (controller.renderPositions()) {
            drawPositions(controller.getPositions(), 1, Color.RED);
        }
    }

    public void setup(TestController controller, Object[] userObjects) {
        this.controller = controller;
        if (userObjects != null && userObjects.length > 0) {
            userDataRenderer = new UserDataRenderer(this, userObjects);
        }
        actual = (TerrainShapeManager) SimpleTestEnvironment.readField("terrainShape", terrainService);
    }

    public void setMoveUserDataRenderer(Object[] userObjects) {
        moveUserDataRenderer = userObjects != null ? new UserDataRenderer(this, userObjects) : null;
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

    public void recordTrail() {
        syncItemContainerService.iterateOverItems(false, false, null, syncItem -> {
            if (!(syncItem instanceof SyncBaseItem)) {
                return null;
            }
            AbstractSyncPhysical phys = syncItem.getAbstractSyncPhysical();
            if (!phys.canMove()) {
                return null;
            }
            Deque<DecimalPosition> trail = trailByItemId.computeIfAbsent(syncItem.getId(), k -> new ArrayDeque<>(TRAIL_MAX));
            trail.add(phys.getPosition());
            while (trail.size() > TRAIL_MAX) {
                trail.pollFirst();
            }
            return null;
        });
    }

    public void clearTrail() {
        trailByItemId.clear();
    }

    private void renderTrail() {
        gc.setStroke(TRAIL_COLOR);
        gc.setLineWidth(LINE_WIDTH);
        for (Deque<DecimalPosition> trail : trailByItemId.values()) {
            DecimalPosition prev = null;
            for (DecimalPosition pos : trail) {
                if (prev != null) {
                    gc.strokeLine(prev.getX(), prev.getY(), pos.getX(), pos.getY());
                }
                prev = pos;
            }
        }
    }

    private void renderShapeTerrainType(Rectangle2D viewport) {
        int x0 = (int) Math.floor(viewport.getStart().getX());
        int y0 = (int) Math.floor(viewport.getStart().getY());
        int x1 = (int) Math.ceil(viewport.getEnd().getX());
        int y1 = (int) Math.ceil(viewport.getEnd().getY());
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                try {
                    TerrainType terrainType = terrainService.getTerrainAnalyzer().getTerrainType(terrainPositionToNodeIndex(samplePosition));
                    gc.setFill(color4TerrainType(terrainType));
                } catch (Exception e) {
                    gc.setFill(Color.RED);
                }
                gc.fillRect(x, y, 1, 1);
            }
        }
    }

    private void renderShapeTerrainHeight(Rectangle2D viewport) {
        int x0 = (int) Math.floor(viewport.getStart().getX());
        int y0 = (int) Math.floor(viewport.getStart().getY());
        int x1 = (int) Math.ceil(viewport.getEnd().getX());
        int y1 = (int) Math.ceil(viewport.getEnd().getY());
        double zoom = 3;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                try {
                    double height = terrainService.getTerrainAnalyzer().getHeightNodeAt(terrainPositionToNodeIndex(samplePosition));
                    double color = MathHelper.clamp(height / zoom, 0, 1);
                    gc.setFill(new Color(color, color, color, 1));
                } catch (Exception e) {
                    gc.setFill(Color.RED);
                }
                gc.fillRect(x, y, 1, 1);
            }
        }
    }

    private void renderShapeTerrainObjects() {
        if (actual == null) {
            return;
        }
        gc.setFill(Color.BROWN);
        for (int tx = 0; tx < actual.getTileXCount(); tx++) {
            for (int ty = 0; ty < actual.getTileYCount(); ty++) {
                com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile tile = getTerrainShapeTile(new Index(tx, ty));
                if (tile == null) {
                    continue;
                }
                NativeTerrainShapeObjectList[] lists = tile.getNativeTerrainShapeObjectLists();
                if (lists == null) {
                    continue;
                }
                for (NativeTerrainShapeObjectList list : lists) {
                    if (list.terrainShapeObjectPositions == null) {
                        continue;
                    }
                    double radius = terrainTypeService.getTerrainObjectConfig(list.terrainObjectConfigId).getRadius();
                    for (NativeTerrainShapeObjectPosition pos : list.terrainShapeObjectPositions) {
                        gc.fillOval(pos.x - radius, pos.y - radius, 2.0 * radius, 2.0 * radius);
                    }
                }
            }
        }
    }

    private com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile getTerrainShapeTile(Index tileIndex) {
        try {
            Field field = TerrainShapeManager.class.getDeclaredField("terrainShapeTiles");
            field.setAccessible(true);
            com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile[][] tiles = (com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile[][]) field.get(actual);
            field.setAccessible(false);
            return tiles[tileIndex.getX()][tileIndex.getY()];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderAStarClosedList() {
        AStar aStar = DebugHelperStatic.getLastAStar();
        if (aStar == null) {
            return;
        }
        gc.setFill(CLOSED_LIST_COLOR);
        Collection<PathingNodeWrapper> nodes = aStar.getClosedListNodes();
        for (PathingNodeWrapper node : nodes) {
            DecimalPosition center = node.getCenter();
            gc.fillRect(center.getX() - 0.5, center.getY() - 0.5, 0.9, 0.9);
        }
        gc.setFill(CLOSED_LIST_START_COLOR);
        DecimalPosition start = aStar.getStartNode().getCenter();
        gc.fillOval(start.getX() - 1.0, start.getY() - 1.0, 2.0, 2.0);
        gc.setFill(CLOSED_LIST_END_COLOR);
        DecimalPosition end = aStar.getDestinationNode().getCenter();
        gc.fillOval(end.getX() - 1.0, end.getY() - 1.0, 2.0, 2.0);
    }

    private void renderSyncItems() {
        syncItemContainerService.iterateOverItems(false, true, null, syncItem -> {
            drawSyncItem(syncItem);
            return null;
        });
    }

    public void drawSyncItem(SyncItem syncItem) {
        AbstractSyncPhysical abstractSyncPhysical = syncItem.getAbstractSyncPhysical();
        if (!abstractSyncPhysical.hasPosition()) {
            return;
        }
        DecimalPosition position = abstractSyncPhysical.getPosition();
        Color color;
        if (syncItem instanceof SyncBaseItem) {
            color = BASE_ITEM_TYPE_COLOR;
        } else if (syncItem instanceof SyncResourceItem) {
            color = RESOURCE_ITEM_TYPE_COLOR;
        } else if (syncItem instanceof SyncBoxItem) {
            color = BOX_ITEM_TYPE_COLOR;
        } else {
            throw new IllegalArgumentException("Unknown SyncItem: " + syncItem);
        }
        gc.setFill(color);
        if (abstractSyncPhysical.canMove()) {
            fillDirectionMarker(position, abstractSyncPhysical.getRadius(), abstractSyncPhysical.getAngle());
            gc.setStroke(BASE_ITEM_TYPE_LINE_COLOR);
            gc.setLineWidth(0.1);
            strokeDirectionMarker(position, abstractSyncPhysical.getRadius(), abstractSyncPhysical.getAngle());
            gc.setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
            gc.setLineWidth(0.5);
            createHeadingLine(position, abstractSyncPhysical.getRadius(), abstractSyncPhysical.getAngle());
        } else {
            gc.fillOval(position.getX() - abstractSyncPhysical.getRadius(), position.getY() - abstractSyncPhysical.getRadius(), abstractSyncPhysical.getRadius() * 2, abstractSyncPhysical.getRadius() * 2);
        }
        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (syncBaseItem.getAbstractSyncPhysical().canMove()) {
                Path path = syncBaseItem.getSyncPhysicalMovable().getPath();
                if (path != null && path.getCurrentWayPoint() != null) {
                    strokeCurveDecimalPosition(path.getWayPositions(), 0.1, Color.CADETBLUE, true);
                    gc.setStroke(Color.BLUEVIOLET);
                    gc.setLineWidth(0.5);
                    gc.strokeLine(position.getX(), position.getY(), path.getCurrentWayPoint().getX(), path.getCurrentWayPoint().getY());
                }
            }
        }
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
        for (int i = 0; i < polygon.size(); i++) {
            xCorners[i] = polygon.get(i).getX();
            yCorners[i] = polygon.get(i).getY();
        }
        gc.fillPolygon(xCorners, yCorners, polygon.size());
    }

    public void fillRectangle(Rectangle2D rectangle, Color color) {
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.8));
        gc.fillRect(rectangle.getStart().getX(), rectangle.getStart().getY(), rectangle.width(), rectangle.height());
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

    private void fillTriangle(double[] groundVertices, int index1, int index2, int index3) {
        double[] xCorners = new double[]{groundVertices[index1], groundVertices[index2], groundVertices[index3]};
        double[] yCorners = new double[]{groundVertices[index1 + 1], groundVertices[index2 + 1], groundVertices[index3 + 1]};
        gc.fillPolygon(xCorners, yCorners, 3);
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
        gc.fillPolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()},
                new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
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
        gc.strokePolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()},
                new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void createHeadingLine(DecimalPosition middle, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        DecimalPosition point1 = middle.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = middle.getPointWithDistance(angel2, radius);
        gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
}
