package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
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
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.DiffTriangleElement;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.system.debugtool.DebugStaticStorage;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Created by Beat
 * on 30.06.2017.
 */
@Singleton
public class WeldTestRenderer extends AbstractTerrainTestRenderer {
    private static final Color BASE_ITEM_TYPE_COLOR = new Color(0, 0, 1, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR = new Color(0, 0.3, 0, 1);
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
    private SyncItemContainerService syncItemContainerService;
    private TerrainShape actual;
    private TerrainShapeTile[][] terrainShapeTiles;
    private UserDataRenderer userDataRenderer;
    private UserDataRenderer moveUserDataRenderer;
    private double zMin = 0;
    private double zMax = 20;
    private WeldTestController weldTestController;

    public void setup(WeldTestController weldTestController, Object[] userObjects) {
        this.weldTestController = weldTestController;
        if (userObjects != null && userObjects.length > 0) {
            userDataRenderer = new UserDataRenderer(this, userObjects);
        }
        actual = (TerrainShape) SimpleTestEnvironment.readField("terrainShape", terrainService);
        try {
            Field field = TerrainShape.class.getDeclaredField("terrainShapeTiles");
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

    @Override
    protected void doRender() {
        Index fromTileIndex = new Index(0, 0);
        Index toTileIndex = fromTileIndex.add(2, 2);

        if (weldTestController.renderTerrainTileSplattings() || weldTestController.renderTerrainTileWater() || weldTestController.renderTerrainTileGround() || weldTestController.renderTerrainTileSlope() || weldTestController.renderTerrainTileHeight() || weldTestController.renderTerrainTileTerrainType() || weldTestController.renderTerrainTileTerrainObject()) {
            doRenderTile(fromTileIndex, toTileIndex);
        }

        if (weldTestController.renderShapeAccess()) {
            renderTerrainShapeAccess();
        }

        // renderTerrainPathingSurfaceAccess();
        if (weldTestController.renderShapeTerrainType() || weldTestController.renderShapeTerrainHeight() || weldTestController.renderShapeFractionalSlope() || weldTestController.renderShapeObstacles() || weldTestController.renderGroundSlopeConnections() || weldTestController.renderShapeWater() || weldTestController.renderShapeTerrainObject()) {
            doRenderShape();
        }
        renderItemTypes();
        if (userDataRenderer != null) {
            userDataRenderer.render();
        }
        if (moveUserDataRenderer != null) {
            moveUserDataRenderer.render();
        }
        if (DebugStaticStorage.getPolygon() != null) {
            strokePolygon(DebugStaticStorage.getPolygon(), FAT_LINE_WIDTH, Color.BLUE, true);
        }
        if (DebugStaticStorage.getPositions() != null) {
            drawPositions(DebugStaticStorage.getPositions(), FAT_LINE_WIDTH, Color.RED);
        }
    }

    private void renderTerrainShapeAccess() {
        DecimalPosition from = new DecimalPosition(0, 0);
        double length = 300;

        for (double x = from.getX(); x < from.getX() + length; x++) {
            for (double y = from.getY(); y < from.getY() + length; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                // Z
                double z = terrainService.getSurfaceAccess().getInterpolatedZ(samplePosition);
                getGc().setFill(color4Z(z));
                getGc().fillRect(x, y, 0.5, 0.5);
                // Norm
                Vertex norm = terrainService.getSurfaceAccess().getInterpolatedNorm(samplePosition);
                if (MathHelper.compareWithPrecision(norm.magnitude(), 1.0)) {
                    getGc().setFill(color4Norm(norm));
                } else {
                    getGc().setFill(Color.BLACK);
                }
                getGc().fillRect(x + 0.5, y, 0.5, 0.5);
                // TerrainType
                TerrainType terrainType = terrainService.getPathingAccess().getTerrainType(samplePosition);
                getGc().setFill(color4TerrainType(terrainType));
                getGc().fillRect(x + 0.5, y + 0.5, 0.5, 0.5);
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
        if (weldTestController.renderTerrainTileSplattings()) {
            renderTileSplatting(terrainTile);
        }

        if (weldTestController.renderTerrainTileWater()) {
            getGc().setLineWidth(LINE_WIDTH);
            if (terrainTile.getTerrainWaterTile() != null) {
                drawTerrainWaterTile(terrainTile.getTerrainWaterTile());
            }
        }
        if (weldTestController.renderTerrainTileGround()) {
            getGc().setLineWidth(LINE_WIDTH);
            for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
                int vertexScalarIndex = vertexIndex * 3;
                fillTriangle(terrainTile.getGroundVertices(), terrainTile.getGroundNorms(), terrainTile.getGroundTangents(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);
                strokeZTriangle(terrainTile.getGroundVertices(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);
            }
        }

        if (weldTestController.renderTerrainTileTerrainType() || weldTestController.renderTerrainTileHeight()) {
            drawNodes(terrainTile.getTerrainNodes(), terrainTile.getIndexX(), terrainTile.getIndexY());
        }

        if (weldTestController.renderTerrainTileSlope()) {
            getGc().setLineWidth(LINE_WIDTH);
            if (terrainTile.getTerrainSlopeTiles() != null) {
                for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                    drawTerrainSlopeTile(terrainSlopeTile);
                }
            }
        }

        if (weldTestController.renderTerrainTileTerrainObject()) {
            if (terrainTile.getTerrainTileObjectLists() != null) {
                getGc().setFill(Color.BROWN);
                Arrays.stream(terrainTile.getTerrainTileObjectLists()).forEach(terrainTileObjectList -> {
                    if (terrainTileObjectList.getModels() != null) {
                        TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.getTerrainObjectConfigId());
                        Arrays.stream(terrainTileObjectList.getModels()).forEach(nativeMatrix -> {
                            NativeVertexDto br = nativeMatrix.multiplyVertex(NativeUtil.toNativeVertex(-terrainObjectConfig.getRadius(), -terrainObjectConfig.getRadius(), 0), 1.0);
                            NativeVertexDto tl = nativeMatrix.multiplyVertex(NativeUtil.toNativeVertex(terrainObjectConfig.getRadius(), terrainObjectConfig.getRadius(), 0), 1.0);
                            Rectangle2D rect = Rectangle2D.generateRectangleFromAnyPoints(new DecimalPosition(br.x, br.y), new DecimalPosition(tl.x, tl.y)); // If rotated, it is may upside down
                            getGc().fillOval(rect.startX(), rect.startY(), rect.width(), rect.height());
                        });
                    }
                });
            }
        }
    }

    private void fillTriangle(double[] groundVertices, double[] groundNorms, double[] groundTangents, int index1, int index2, int index3) {
        int fillIndex = index2;
        double[] fillVertices = groundNorms;
        getGc().setFill(color4Norm(new Vertex(fillVertices[fillIndex], fillVertices[fillIndex + 1], fillVertices[fillIndex + 2])));
        double[] xCorners = new double[]{groundVertices[index1], groundVertices[index2], groundVertices[index3]};
        double[] yCorners = new double[]{groundVertices[index1 + 1], groundVertices[index2 + 1], groundVertices[index3 + 1]};
        getGc().fillPolygon(xCorners, yCorners, 3);
    }

    private void fillTriangle(double[] groundVertices, int index1, int index2, int index3) {
        double[] xCorners = new double[]{groundVertices[index1], groundVertices[index2], groundVertices[index3]};
        double[] yCorners = new double[]{groundVertices[index1 + 1], groundVertices[index2 + 1], groundVertices[index3 + 1]};
        getGc().fillPolygon(xCorners, yCorners, 3);
    }

    public void showDifference(DiffTriangleElement diffTriangleElement) {
        switch (diffTriangleElement.getDifference()) {
            case XY:
                getGc().setLineWidth(LINE_WIDTH);
                getGc().setStroke(Color.RED);
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case Z:
                getGc().setFill(new Color(1.0, 0.5, 0.5, 0.5));
                fillTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case XYZ:
                getGc().setFill(new Color(1.0, 0.5, 0.5, 0.5));
                fillTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                getGc().setLineWidth(LINE_WIDTH);
                getGc().setStroke(Color.RED);
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case MISSING:
                getGc().setLineWidth(LINE_WIDTH);
                getGc().setStroke(new Color(1.0, 0.8, 0.0, 1.0));
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            case UNEXPECTED:
                getGc().setLineWidth(LINE_WIDTH);
                getGc().setStroke(new Color(0.8, 0, 0.8, 1.0));
                strokeTriangle(diffTriangleElement.getVertices(), diffTriangleElement.getScalarIndex(), diffTriangleElement.getScalarIndex() + 3, diffTriangleElement.getScalarIndex() + 6);
                break;
            default:
                throw new IllegalArgumentException("Unknown Difference: " + diffTriangleElement.getDifference());
        }
    }

    private void strokeTriangle(double[] vertices, int index1, int index2, int index3) {
        getGc().strokeLine(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1]);
        getGc().strokeLine(vertices[index2], vertices[index2 + 1], vertices[index3], vertices[index3 + 1]);
        getGc().strokeLine(vertices[index3], vertices[index3 + 1], vertices[index1], vertices[index1 + 1]);
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
        getGc().setStroke(new LinearGradient(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1], false, CycleMethod.NO_CYCLE, new Stop(0, color1), new Stop(1, color2)));
        getGc().strokeLine(vertices[index1], vertices[index1 + 1], vertices[index2], vertices[index2 + 1]);
    }

    private Color color4Z(double z) {
        double value = InterpolationUtils.interpolate(0, 1, zMin, zMax, z);
        value = MathHelper.clamp(value, 0, 1);
        return Color.color(value, 0, value);
    }

    private void drawTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        getGc().setLineWidth(LINE_WIDTH);
        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;
            // fillTriangle(terrainSlopeTile.getVertices(), terrainSlopeTile.getNorms(), terrainSlopeTile.getTangents(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);
            strokeZTriangle(terrainSlopeTile.getVertices(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6);

//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//            getGc().setStroke(Color.GRAY);
//            getGc().strokePolygon(xCorners, yCorners, 3);
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
    }

    private void renderTileSplatting(TerrainTile terrainTile) {
        getGc().setLineWidth(FAT_LINE_WIDTH);

        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;
            Color color1 = Color.color(0, terrainTile.getGroundSplattings()[vertexIndex], 0);
            Color color2 = Color.color(0, terrainTile.getGroundSplattings()[vertexIndex + 1], 0);
            Color color3 = Color.color(0, terrainTile.getGroundSplattings()[vertexIndex + 2], 0);
            strokeGradientTriangle(terrainTile.getGroundVertices(), vertexScalarIndex, vertexScalarIndex + 3, vertexScalarIndex + 6, color1, color2, color3);
        }

        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex++) {
                    int vertexScalarIndex = vertexIndex * 3;

                    double xCorner = terrainSlopeTile.getVertices()[vertexScalarIndex];
                    double yCorner = terrainSlopeTile.getVertices()[vertexScalarIndex + 1];

                    double splatting = terrainSlopeTile.getGroundSplattings()[vertexIndex];

                    DecimalPosition position = new DecimalPosition(xCorner, yCorner);
                    DecimalPosition splattingAsPosition = position.getPointWithDistance(MathHelper.QUARTER_RADIANT, splatting * 8);
                    getGc().strokeLine(position.getX(), position.getY(), splattingAsPosition.getX(), splattingAsPosition.getY());
                }
            }
        }
    }

    private void drawTerrainWaterTile(TerrainWaterTile terrainWaterTile) {
        getGc().setLineWidth(LINE_WIDTH);
        getGc().setStroke(Color.BLUE);
        for (int vertexIndex = 0; vertexIndex < terrainWaterTile.getVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex], terrainWaterTile.getVertices()[vertexScalarIndex + 3], terrainWaterTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainWaterTile.getVertices()[vertexScalarIndex + 1], terrainWaterTile.getVertices()[vertexScalarIndex + 4], terrainWaterTile.getVertices()[vertexScalarIndex + 7]};
            getGc().strokePolygon(xCorners, yCorners, 3);
            //getGc().setFill(Color.color(1, 0, 0, 0.3));
            //getGc().fillPolygon(xCorners, yCorners, 3);
        }
    }


    private void drawNodes(TerrainNode[][] terrainNodes, int indexX, int indexY) {
        if (terrainNodes == null) {
            return;
        }
        for (int x = 0; x < TerrainUtil.TERRAIN_TILE_NODES_COUNT; x++) {
            for (int y = 0; y < TerrainUtil.TERRAIN_TILE_NODES_COUNT; y++) {
                TerrainNode terrainNode = terrainNodes[x][y];
                if (terrainNode != null) {
                    DecimalPosition absoluteNodePosition = TerrainUtil.toTileAbsolute(new Index(indexX, indexY)).add(TerrainUtil.toNodeAbsolute(new Index(x, y)));
                    drawNode(terrainNode, absoluteNodePosition);
                }
            }
        }
    }

    private void drawNode(TerrainNode terrainNode, DecimalPosition absoluteNodePosition) {
        if (weldTestController.renderTerrainTileTerrainType()) {
            TerrainType terrainType = TerrainType.fromOrdinal(terrainNode.getTerrainType());
            if (terrainType != null) {
                getGc().setFill(color4TerrainType(terrainType));
                getGc().fillRect(absoluteNodePosition.getX(), absoluteNodePosition.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
            }
        }
        if (weldTestController.renderTerrainTileHeight()) {
            getGc().setFill(color4Z(terrainNode.getHeight()));
            getGc().fillRect(absoluteNodePosition.getX(), absoluteNodePosition.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
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
                getGc().setFill(color4TerrainType(terrainType));
                getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength - 0.1, subNodeLength - 0.1);
            }
        }
        if (weldTestController.renderTerrainTileHeight()) {
            if (terrainSubNode.getHeight() != null) {
                getGc().setFill(color4Z(terrainSubNode.getHeight()));
                getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength - 0.1, subNodeLength - 0.1);
            }
        }

//        getGc().setStroke(new Color(0, 0, 1, 1));
//        getGc().strokeRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//        if (terrainSubNode.getTerrainSubNodes() == null) {
//            if (terrainSubNode.isLand() == null || !terrainSubNode.isLand()) {
//                getGc().setFill(new Color(1, 0, 0, 0.5));
//                getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//            }
//
//            double height = terrainSubNode.getHeight();
//            double v = (height + 5) / 25.0;
//            getGc().setFill(Color.color(v, v, v, 0.5));
//            getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), subNodeLength, subNodeLength);
//        }
        drawSubNodes(terrainSubNode.getTerrainSubNodes(), absolutePosition, depth + 1);

    }

    private void doRenderShape() {
        for (int x = 0; x < actual.getTileXCount(); x++) {
            for (int y = 0; y < actual.getTileYCount(); y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    displayTerrainShapeTile(new Index(x, y).add(actual.getTileOffset()), terrainShapeTile);
                }
            }
        }
    }

    private void renderItemTypes() {
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
//                getGc().setFill(new Color(0, 1, 1, 0.3));
//                getGc().fillRect(rect.startX(), rect.startY(), rect.width() - 0.1, rect.height() - 0.1);
//            }
//            if(pathingNodeWrapper.getTerrainShapeSubNode() != null) {
//                double length = TerrainUtil.calculateSubNodeLength(pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//                getGc().setFill(new Color(1, 0, 1, 0.3));
//                getGc().fillRect(pathingNodeWrapper.getSubNodePosition().getX(), pathingNodeWrapper.getSubNodePosition().getY(), length - 0.1, length - 0.1);
//            }
//        }
//    }

    private void displayTerrainShapeTile(Index tileIndex, TerrainShapeTile terrainShapeTile) {
        getGc().setLineWidth(LINE_WIDTH * 4.0);
        getGc().setStroke(Color.DARKGREEN);
        DecimalPosition absolute = TerrainUtil.toTileAbsolute(tileIndex);
        getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH);
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
        getGc().setLineWidth(LINE_WIDTH);
        getGc().setStroke(Color.BLACK);
        getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
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
            getGc().setFill(color4TerrainType(terrainShapeNode.getTerrainType()));
            getGc().fillRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
        if (weldTestController.renderShapeTerrainHeight() && terrainShapeNode.getGameEngineHeightOrNull() != null) {
            getGc().setFill(color4Z(terrainShapeNode.getGameEngineHeightOrNull()));
            getGc().fillRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
        displaySubNodes(0, absolute, terrainShapeNode.getTerrainShapeSubNodes());
    }

    private void displayGroundSlopeConnections(List<List<Vertex>> groundSlopeConnections) {
        if (groundSlopeConnections == null) {
            return;
        }
        for (List<Vertex> groundSlopeConnection : groundSlopeConnections) {
            strokeVertexPolygon(groundSlopeConnection, LINE_WIDTH, Color.GREEN, true);
        }
    }

    private void displayShapeWater(List<List<Vertex>> waterSegments) {
        if (waterSegments == null) {
            return;
        }
        for (List<Vertex> waterSegment : waterSegments) {
            strokeVertexPolygon(waterSegment, LINE_WIDTH, Color.BLUE, true);
        }
    }

    private void displayObstacles(TerrainShapeNode terrainShapeNode) {
        if (terrainShapeNode.getObstacles() == null) {
            return;
        }
        for (Obstacle obstacle : terrainShapeNode.getObstacles()) {
            if (obstacle instanceof ObstacleSlope) {
                ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
                getGc().setStroke(Color.RED);
                getGc().strokeLine(obstacleSlope.getLine().getPoint1().getX(), obstacleSlope.getLine().getPoint1().getY(), obstacleSlope.getLine().getPoint2().getX(), obstacleSlope.getLine().getPoint2().getY());
            } else if (obstacle instanceof ObstacleTerrainObject) {
                ObstacleTerrainObject obstacleTerrainObject = (ObstacleTerrainObject) obstacle;
                getGc().setStroke(Color.RED);
                getGc().fillOval(obstacleTerrainObject.getCircle().getCenter().getX() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getCenter().getY() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius());
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
        getGc().setStroke(Color.BLUEVIOLET);
        getGc().setLineWidth(LINE_WIDTH);
        getGc().strokeRect(absolute.getX(), absolute.getY(), subLength, subLength);
        displaySubNodes(depth + 1, absolute, terrainShapeSubNode.getTerrainShapeSubNodes());
        if (weldTestController.renderShapeTerrainType() && terrainShapeSubNode.getTerrainType() != null) {
            getGc().setFill(color4TerrainType(terrainShapeSubNode.getTerrainType()));
            getGc().fillRect(absolute.getX(), absolute.getY(), subLength - 0.1, subLength - 0.1);
        }
        if (weldTestController.renderShapeTerrainHeight() && terrainShapeSubNode.getHeight() != null) {
            getGc().setFill(color4Z(terrainShapeSubNode.getHeight()));
            getGc().fillRect(absolute.getX(), absolute.getY(), subLength - 0.1, subLength - 0.1);
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
        getGc().setStroke(Color.BROWN);
        getGc().setLineWidth(FAT_LINE_WIDTH);
        Arrays.stream(nativeTerrainShapeObjectLists).forEach(nativeTerrainShapeObjectList -> {
            if (nativeTerrainShapeObjectList.positions != null) {
                double radius = terrainTypeService.getTerrainObjectConfig(nativeTerrainShapeObjectList.terrainObjectId).getRadius();
                Arrays.stream(nativeTerrainShapeObjectList.positions).forEach(nativeTerrainShapeObjectPosition -> {
                    double correctedRadius = radius * nativeTerrainShapeObjectPosition.scale;
                    getGc().strokeOval(nativeTerrainShapeObjectPosition.x - correctedRadius, nativeTerrainShapeObjectPosition.y - correctedRadius, 2.0 * correctedRadius, 2.0 * correctedRadius);
                });
            }
        });
    }

    public void drawSyncItem(SyncItem syncItem) {
        SyncPhysicalArea syncPhysicalArea = syncItem.getSyncPhysicalArea();
        if (!syncPhysicalArea.hasPosition()) {
            return;
        }
        DecimalPosition position = syncPhysicalArea.getPosition2d();
        if (syncItem instanceof SyncBaseItem) {
            getGc().setFill(BASE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncResourceItem) {
            getGc().setFill(RESOURCE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncBoxItem) {
            getGc().setFill(BOX_ITEM_TYPE_COLOR);
        } else {
            throw new IllegalArgumentException("Unknown SyncItem: " + syncItem);
        }
        if (syncItem.getSyncPhysicalArea().canMove()) {
            fillPolygon(syncItem);
            getGc().setStroke(BASE_ITEM_TYPE_LINE_COLOR);
            getGc().setLineWidth(0.1);
            strokePolygon(syncItem);
            getGc().setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
            getGc().setLineWidth(0.5);
            createHeadingLine(syncItem);
        } else {
            getGc().fillOval(position.getX() - syncPhysicalArea.getRadius(), position.getY() - syncPhysicalArea.getRadius(), syncPhysicalArea.getRadius() * 2, syncPhysicalArea.getRadius() * 2);
        }

        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (syncBaseItem.getSyncWeapon() != null) {
                Matrix4 matrix4 = syncBaseItem.getSyncWeapon().createTurretMatrix();
                DecimalPosition canonStart = matrix4.multiply(Vertex.ZERO, 1.0).toXY();
                DecimalPosition canonEnd = matrix4.multiply(syncBaseItem.getSyncWeapon().getWeaponType().getTurretType().getMuzzlePosition(), 1.0).toXY();
                getGc().setStroke(BASE_ITEM_TYPE_WEAPON_COLOR);
                getGc().setLineWidth(0.5);
                getGc().strokeLine(canonStart.getX(), canonStart.getY(), canonEnd.getX(), canonEnd.getY());
            }
            if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                Path path = syncBaseItem.getSyncPhysicalMovable().getPath();
                if (path != null) {
                    strokeCurveDecimalPosition(path.getWayPositions(), 0.1, Color.CADETBLUE, true);
                    getGc().setStroke(Color.BLUEVIOLET);
                    getGc().setLineWidth(0.5);
                    getGc().strokeLine(syncBaseItem.getSyncPhysicalArea().getPosition2d().getX(), syncBaseItem.getSyncPhysicalArea().getPosition2d().getY(), path.getCurrentWayPoint().getX(), path.getCurrentWayPoint().getY());
                }
            }
        }
    }

    private void fillPolygon(SyncItem syncItem) {
        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point3 = middle.getPointWithDistance(angel3, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point4 = middle.getPointWithDistance(angel4, syncItem.getSyncPhysicalArea().getRadius());

        getGc().fillPolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void strokePolygon(SyncItem syncItem) {
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point3 = middle.getPointWithDistance(angel3, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point4 = middle.getPointWithDistance(angel4, syncItem.getSyncPhysicalArea().getRadius());

        getGc().strokePolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void createHeadingLine(SyncItem syncItem) {
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;

        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());

        getGc().strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public void strokeCurveDecimalPosition(List<DecimalPosition> curve, double strokeWidth, Color color, boolean showPoint) {
        getGc().setStroke(color);
        getGc().setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        getGc().setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            DecimalPosition start = curve.get(i);
            if (i + 1 < curve.size()) {
                DecimalPosition end = curve.get(i + 1);
                getGc().strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            }
            if (showPoint) {
                getGc().fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    public void strokeCircle(Circle2D circle2D, double lineWidth, Color color) {
        getGc().setStroke(color);
        getGc().setLineWidth(lineWidth);
        getGc().strokeOval(circle2D.getCenter().getX() - circle2D.getRadius(), circle2D.getCenter().getY() - circle2D.getRadius(), 2.0 * circle2D.getRadius(), 2.0 * circle2D.getRadius());
    }

    public void fillCircle(Circle2D circle2D, Color color) {
        getGc().setFill(color);
        getGc().fillOval(circle2D.getCenter().getX() - circle2D.getRadius(), circle2D.getCenter().getY() - circle2D.getRadius(), 2.0 * circle2D.getRadius(), 2.0 * circle2D.getRadius());
    }

    public void drawPosition(DecimalPosition position, double radius, Color color) {
        getGc().setFill(color);
        getGc().fillOval(position.getX() - radius, position.getY() - radius, radius * 2.0, radius * 2.0);
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
