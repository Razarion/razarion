package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
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
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import com.btxtech.shared.gameengine.planet.terrain.gui.terrainshape.TerrainShapeTestRenderer;
import com.btxtech.shared.utils.MathHelper;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
    private SyncItemContainerService syncItemContainerService;
    private TerrainShape actual;
    private TerrainShapeTile[][] terrainShapeTiles;
    private UserDataRenderer userDataRenderer;

    public void setupFields(Object[] userObjects) {
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

    @Override
    protected void doRender() {
        for (int x = 0; x < actual.getTileXCount(); x++) {
            for (int y = 0; y < actual.getTileYCount(); y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    displayTerrainShapeTile(new Index(x, y).add(actual.getTileOffset()), terrainShapeTile);
                }
            }
        }
//        displayClosedList();
//        if(displayDTO.getPathingNodeWrapper() != null) {
//            getGc().setFill(new Color(1, 0, 0, 1));
//            if(displayDTO.getPathingNodeWrapper().getNodeIndex() != null) {
//                Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(displayDTO.getPathingNodeWrapper().getNodeIndex());
//                getGc().fillRect(rect.startX(), rect.startY(), rect.width() - 0.1, rect.height() - 0.1);
//            } else if(displayDTO.getPathingNodeWrapper().getSubNodePosition() != null) {
//                double length = TerrainUtil.calculateSubNodeLength(displayDTO.getPathingNodeWrapper().getTerrainShapeSubNode().getDepth());
//                getGc().fillRect(displayDTO.getPathingNodeWrapper().getSubNodePosition().getX(), displayDTO.getPathingNodeWrapper().getSubNodePosition().getY(), length, length);
//            }
//        }


        renderItemTypes();

        if (userDataRenderer != null) {
            userDataRenderer.render();
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
        // displayObstacles(terrainShapeNode);
        displayGroundSlopeConnections(terrainShapeNode.getGroundSlopeConnections());
        if (terrainShapeNode.getTerrainType() != null) {
            getGc().setFill(TerrainShapeTestRenderer.color4TerrainType(terrainShapeNode.getTerrainType()));
            getGc().fillRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - 0.1);
        }
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
        if (terrainShapeSubNode.getTerrainType() != null) {
            getGc().setFill(TerrainShapeTestRenderer.color4TerrainType(terrainShapeSubNode.getTerrainType()));
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

    public void drawSyncItem(SyncItem syncItem) {
        SyncPhysicalArea syncPhysicalArea = syncItem.getSyncPhysicalArea();
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

    public void drawPosition(DecimalPosition position, double radius, Color color) {
        getGc().setFill(color);
        getGc().fillOval(position.getX() - radius, position.getY() - radius, radius * 2.0, radius * 2.0);
    }

    public void drawPositions(Collection<DecimalPosition> positions, double radius, Color color) {
        for (DecimalPosition position : positions) {
            drawPosition(position, radius, color);
        }
    }

}
