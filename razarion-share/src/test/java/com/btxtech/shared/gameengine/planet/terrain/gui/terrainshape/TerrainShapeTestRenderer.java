package com.btxtech.shared.gameengine.planet.terrain.gui.terrainshape;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import com.btxtech.shared.utils.InterpolationUtils;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Beat
 * on 30.06.2017.
 */
public class TerrainShapeTestRenderer extends AbstractTerrainTestRenderer {
    private static final DecimalPosition FROM = new DecimalPosition(0, 0);
    private static final double LENGTH = 160;
    private TerrainShape actual;
    private TerrainShapeTile[][] terrainShapeTiles;

    public TerrainShapeTestRenderer(TerrainShape actual) {
        this.actual = actual;
        try {
            Field field = TerrainShape.class.getDeclaredField("terrainShapeTiles");
            field.setAccessible(true);
            terrainShapeTiles = (TerrainShapeTile[][]) field.get(actual);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doRender() {
        renderPathingAccess();
        for (int x = 0; x < actual.getTileXCount(); x++) {
            for (int y = 0; y < actual.getTileYCount(); y++) {
                displayTerrainShapeTile(new Index(x, y).add(actual.getTileOffset()), terrainShapeTiles[x][y]);
            }
        }
    }

    private void displayTerrainShapeTile(Index tileIndex, TerrainShapeTile terrainShapeTile) {
        getGc().setLineWidth(LINE_WIDTH * 4.0);
        getGc().setStroke(Color.DARKGREEN);
        DecimalPosition absolute = TerrainUtil.toTileAbsolute(tileIndex);
        getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH);
        displayNodes(absolute, terrainShapeTile);
        displayFractionalSlope(terrainShapeTile.getFractionalSlopes());
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
        displaySubNodes(0, absolute, terrainShapeNode.getTerrainShapeSubNodes());
        displayObstacles(terrainShapeNode);
        //displayGroundSlopeConnections(terrainShapeNode.getGroundSlopeConnections());
    }

    private void displayGroundSlopeConnections(List<List<Vertex>> groundSlopeConnections) {
        if (groundSlopeConnections == null) {
            return;
        }
        for (List<Vertex> groundSlopeConnection : groundSlopeConnections) {
            strokeVertexPolygon(groundSlopeConnection, LINE_WIDTH, Color.GREEN, true);
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
                getGc().strokeOval(obstacleTerrainObject.getCircle().getCenter().getX() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getCenter().getY() - obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius(), obstacleTerrainObject.getCircle().getRadius() + obstacleTerrainObject.getCircle().getRadius());
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
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
//            if (terrainShapeSubNode.isLand()) {
//                getGc().setFill(new Color(0.0f, 0.8f, 0.0f, 0.5f));
//                getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
//            } else {
//                getGc().setFill(new Color(0.8f, 0.0f, 0.0f, 0.5f));
//                getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
//            }
//            if (terrainShapeSubNode.getHeight() != null) {
//                double v = terrainShapeSubNode.getHeight() / 20.0;
//                getGc().setFill(new Color(v, v, v, 1f));
//                getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
//            }
        }
        getGc().setStroke(Color.BLUEVIOLET);
        getGc().setLineWidth(LINE_WIDTH);
        getGc().strokeRect(absolute.getX(), absolute.getY(), subLength, subLength);
        displaySubNodes(depth + 1, absolute, terrainShapeSubNode.getTerrainShapeSubNodes());
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


    private void renderPathingAccess() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double x = FROM.getX(); x < FROM.getX() + LENGTH; x++) {
            for (double y = FROM.getY(); y < FROM.getY() + LENGTH; y++) {
                double z = actual.getSurfaceAccess().getInterpolatedZ(new DecimalPosition(x + 0.5, y + 0.5));
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
                double z = actual.getSurfaceAccess().getInterpolatedZ(samplePosition);
                TerrainType terrainType = actual.getPathingAccess().getTerrainType(samplePosition);
                double v = InterpolationUtils.interpolate(0.0, 1.0, min, max, z);
                getGc().setFill(new Color(v, v, v, 1));
                getGc().fillRect(x, y, 1, 1);
                switch (terrainType) {
                    case LAND:
                        getGc().setFill(Color.GREEN);
                        break;
                    case WATER:
                        getGc().setFill(Color.BLUE);
                        break;
                    case LAND_COST:
                        getGc().setFill(Color.LIGHTGREEN);
                        break;
                    case WATER_COST:
                        getGc().setFill(Color.SANDYBROWN);
                        break;
                    case BLOCKED:
                        getGc().setFill(Color.RED);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown terrainType: " + terrainType);
                }
                getGc().fillRect(x, y, 0.6, 0.6);
            }
        }
    }


}
