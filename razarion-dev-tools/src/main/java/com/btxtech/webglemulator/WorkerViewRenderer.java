package com.btxtech.webglemulator;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import javafx.scene.paint.Color;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.12.2016.
 */
public class WorkerViewRenderer extends Abstract2dRenderer {
    private static final DecimalPosition FROM = new DecimalPosition(0, 0);
    private static final double LENGTH = 400;
    // private static final DecimalPosition FROM = new DecimalPosition(208, 148);
    // private static final double LENGTH = 4;
    private static final double LINE_WIDTH = 0.1;
    @Inject
    private TerrainService terrainService;
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;
    @Inject
    private TerrainTypeService terrainTypeService;
    private List<TerrainSlopePosition> terrainSlopePositions;
    private TerrainShape terrainShape;
    private TerrainShapeTile[][] terrainShapeTiles;
    private String display;

    @PostConstruct
    public void postConstruct() {
        terrainSlopePositions = jsonProviderEmulator.readSlopes(terrainService.getPlanetConfig().getPlanetId());
        readTerrainShapeTiles();
    }

    private void readTerrainShapeTiles() {
        try {
            Field field = TerrainService.class.getDeclaredField("terrainShape");
            field.setAccessible(true);
            terrainShape = (TerrainShape) field.get(terrainService);
            field.setAccessible(false);

            field = TerrainShape.class.getDeclaredField("terrainShapeTiles");
            field.setAccessible(true);
            terrainShapeTiles = (TerrainShapeTile[][]) field.get(terrainShape);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void render() {
        preRender();

        ExtendedGraphicsContext egc = createExtendedGraphicsContext();
        if (WorkerViewController.ACCESS_DISPLAY.equals(display)) {
            renderTerrainShapeAccess(egc);
        } else if (WorkerViewController.TILE_DISPLAY.equals(display)) {
            renderTerrainShapeTiles(egc);
        } else if (WorkerViewController.BOTH_DISPLAY.equals(display)) {
            renderTerrainShapeAccess(egc);
            renderTerrainShapeTiles(egc);
        }
        // renderSlopes(egc);
        postRender();
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    private void renderTerrainShapeAccess(ExtendedGraphicsContext egc) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double x = FROM.getX(); x < FROM.getX() + LENGTH; x++) {
            for (double y = FROM.getY(); y < FROM.getY() + LENGTH; y++) {
                double z = terrainService.getSurfaceAccess().getInterpolatedZ(new DecimalPosition(x + 0.5, y + 0.5));
                if (z > max) {
                    max = z;
                }
                if (z < min) {
                    min = z;
                }
            }
        }

        for (double x = FROM.getX(); x < FROM.getX() + LENGTH; x++) {
            for (double y = FROM.getY(); y < FROM.getY() + LENGTH; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                double z = terrainService.getSurfaceAccess().getInterpolatedZ(samplePosition);
                // boolean free = terrainService.getPathingAccess().isTerrainFree(samplePosition);
//                double v = InterpolationUtils.interpolate(0.0, 1.0, min, max, z);
//                if (v < 0) {
//                    v = 0;
//                }
//                egc.getGc().setFill(new Color(v, v, v, 1));
//                egc.getGc().fillRect(x, y, 1, 1);
//                if (free) {
//                    egc.getGc().setFill(Color.GREEN);
//                } else {
//                    egc.getGc().setFill(Color.RED);
//                }
//                egc.getGc().fillRect(x, y, 0.6, 0.6);
            }
        }
    }

    private void renderTerrainShapeTiles(ExtendedGraphicsContext egc) {
        for (int x = 0; x < terrainShape.getTileXCount(); x++) {
            for (int y = 0; y < terrainShape.getTileYCount(); y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    displayTerrainShapeTile(egc, new Index(x, y).add(terrainShape.getTileOffset()), terrainShapeTile);
                }
            }
        }
    }

    private void displayTerrainShapeTile(ExtendedGraphicsContext egc, Index tileIndex, TerrainShapeTile terrainShapeTile) {
        egc.getGc().setLineWidth(LINE_WIDTH * 4.0);
        egc.getGc().setStroke(Color.GREENYELLOW);
        DecimalPosition absolute = TerrainUtil.toTileAbsolute(tileIndex);
        egc.getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH);
        displayNodes(egc, absolute, terrainShapeTile);
        // displayFractionalSlope(egc, terrainShapeTile.getFractionalSlopes());
    }

    private void displayNodes(ExtendedGraphicsContext egc, DecimalPosition absoluteTile, TerrainShapeTile terrainShapeTile) {
        if (!terrainShapeTile.hasNodes()) {
            return;
        }
        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null) {
                return;
            }
            displayNode(egc, absoluteTile, nodeRelativeIndex, terrainShapeNode);
        });

    }

    private void displayNode(ExtendedGraphicsContext egc, DecimalPosition absoluteTile, Index nodeRelativeIndex, TerrainShapeNode terrainShapeNode) {
        DecimalPosition absolute = TerrainUtil.toNodeAbsolute(nodeRelativeIndex).add(absoluteTile);
        egc.getGc().setLineWidth(LINE_WIDTH);
        egc.getGc().setStroke(Color.BLACK);
        egc.getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        displaySubNodes(egc, 0, absolute, terrainShapeNode.getTerrainShapeSubNodes());
        displayObstacles(egc, terrainShapeNode);
        //displayGroundSlopeConnections(terrainShapeNode.getGroundSlopeConnections());
    }

    private void displayGroundSlopeConnections(ExtendedGraphicsContext egc, List<List<Vertex>> groundSlopeConnections) {
        if (groundSlopeConnections == null) {
            return;
        }
        for (List<Vertex> groundSlopeConnection : groundSlopeConnections) {
            // strokeVertexPolygon(groundSlopeConnection, LINE_WIDTH, Color.GREEN, true);
        }
    }

    private void displayObstacles(ExtendedGraphicsContext egc, TerrainShapeNode terrainShapeNode) {
        if (terrainShapeNode.getObstacles() == null) {
            return;
        }
        for (Obstacle obstacle : terrainShapeNode.getObstacles()) {
            if (obstacle instanceof ObstacleSlope) {
                ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
                egc.getGc().setStroke(Color.RED);
                egc.getGc().strokeLine(obstacleSlope.getPoint1().getX(), obstacleSlope.getPoint1().getY(), obstacleSlope.getPoint2().getX(), obstacleSlope.getPoint2().getY());
            } else if (obstacle instanceof ObstacleTerrainObject) {
                ObstacleTerrainObject obstacleTerrainObject = (ObstacleTerrainObject) obstacle;
                egc.getGc().setStroke(Color.RED);
                egc.getGc().fillOval(obstacleTerrainObject.getCircle().getCenter().getX() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getCenter().getY() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius());
            } else {
                throw new IllegalArgumentException("Unknown: " + obstacle);
            }
        }
    }

    private void displaySubNodes(ExtendedGraphicsContext egc, int depth, DecimalPosition absolute, TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return;
        }
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        TerrainShapeSubNode bottomLeft = terrainShapeSubNodes[0];
        if (bottomLeft != null) {
            displaySubNode(egc, depth, absolute, bottomLeft);
        }
        TerrainShapeSubNode bottomRight = terrainShapeSubNodes[1];
        if (bottomRight != null) {
            displaySubNode(egc, depth, absolute.add(subLength, 0), bottomRight);
        }
        TerrainShapeSubNode topRight = terrainShapeSubNodes[2];
        if (topRight != null) {
            displaySubNode(egc, depth, absolute.add(subLength, subLength), topRight);
        }
        TerrainShapeSubNode topLeft = terrainShapeSubNodes[3];
        if (topLeft != null) {
            displaySubNode(egc, depth, absolute.add(0, subLength), topLeft);
        }
    }

    private void displaySubNode(ExtendedGraphicsContext egc, int depth, DecimalPosition absolute, TerrainShapeSubNode terrainShapeSubNode) {
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
//            if (terrainShapeSubNode.isLand()) {
//                egc.getGc().setFill(new Color(0.0f, 0.8f, 0.0f, 0.5f));
//                egc.getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
//            } else {
//                egc.getGc().setFill(new Color(0.8f, 0.0f, 0.0f, 0.5f));
//                egc.getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
//            }
//            double v = (terrainShapeSubNode.getHeight() + 10) / 20.0;
//            egc.getGc().setFill(new Color(v, v, v, 1f));
//            egc.getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
        }
        egc.getGc().setStroke(Color.BLUEVIOLET);
        egc.getGc().setLineWidth(LINE_WIDTH);
        egc.getGc().strokeRect(absolute.getX(), absolute.getY(), subLength, subLength);
        displaySubNodes(egc, depth + 1, absolute, terrainShapeSubNode.getTerrainShapeSubNodes());
    }

    private void displayFractionalSlope(ExtendedGraphicsContext egc, List<FractionalSlope> fractionalSlopes) {
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
            egc.strokeCurveDecimalPosition(inner, LINE_WIDTH, Color.PINK, true);
            egc.strokeCurveDecimalPosition(outer, LINE_WIDTH, Color.AQUA, true);
        }
    }
}
