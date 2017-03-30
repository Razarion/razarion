package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.terrain.TerrainConstants;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * 21.01.2017.
 */
@ApplicationScoped
public class ObstacleContainer {
    public static final int TILE_SIZE = 8;
    private ObstacleContainerTile[][] obstacleContainerTiles;
    private DecimalPosition absoluteOffset;
    private int xCount;
    private int yCount;

    public void setup(Rectangle groundMeshDimension, Collection<Slope> slopes, MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions) {
        absoluteOffset = new DecimalPosition(groundMeshDimension.getStart()).multiply(TerrainConstants.GROUND_NODE_EDGE_LENGTH);
        xCount = (int) Math.ceil(groundMeshDimension.width() * TerrainConstants.GROUND_NODE_EDGE_LENGTH / TILE_SIZE);
        yCount = (int) Math.ceil(groundMeshDimension.height() * TerrainConstants.GROUND_NODE_EDGE_LENGTH / TILE_SIZE);
        obstacleContainerTiles = new ObstacleContainerTile[xCount][yCount];
        for (Slope slope : slopes) {
            insertObstacles(slope.generateObstacles());
        }
        terrainObjectConfigPositions.iterate((terrainObject, position) -> {
            insertObstacle(new ObstacleCircle(new Circle2D(position.getPosition(), terrainObject.getRadius())));
            return true;
        });
    }

    private void insertObstacles(Collection<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            insertObstacle(obstacle);
        }
    }

    private void insertObstacle(Obstacle obstacle) {
        if (obstacle instanceof ObstacleLine) {
            insertObstacleLine((ObstacleLine) obstacle);
        } else if (obstacle instanceof ObstacleCircle) {
            insertObstacleCircle((ObstacleCircle) obstacle);
        } else {
            throw new IllegalArgumentException("Can not handle: " + obstacle);
        }
    }

    private void insertObstacleLine(ObstacleLine obstacleLine) {
        for (Index tile : absoluteLineToTiles(obstacleLine.getLine())) {
            getOrCreate(tile).addObstacle(obstacleLine);
        }
    }

    private List<Index> absoluteLineToTiles(Line absoluteLine) {
        Line line = new Line(absoluteLine.getPoint1().sub(absoluteOffset), absoluteLine.getPoint2().sub(absoluteOffset));
        return GeometricUtil.rasterizeLine(line, TILE_SIZE);
    }

    private void insertObstacleCircle(ObstacleCircle obstacleCircle) {
        for (Index tile : absoluteCircleToTiles(obstacleCircle.getCircle())) {
            getOrCreate(tile).addObstacle(obstacleCircle);
        }
    }

    private List<Index> absoluteCircleToTiles(Circle2D absoluteCircle) {
        Circle2D circle = new Circle2D(absoluteCircle.getCenter().sub(absoluteOffset), absoluteCircle.getRadius());
        return GeometricUtil.rasterizeCircle(circle, TILE_SIZE);
    }

    public Iterable<Obstacle> getObstacles(SyncPhysicalArea syncPhysicalArea) {
        List<Index> tiles = absoluteCircleToTiles(new Circle2D(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius()));
        Set<Obstacle> obstacles = new HashSet<>();
        for (Index tile : tiles) {
            ObstacleContainerTile obstacleContainerTile = getObstacleContainerTile(tile);
            if (obstacleContainerTile != null) {
                obstacles.addAll(obstacleContainerTile.getObstacles());
            }
        }
        return obstacles;
    }

    public DecimalPosition toAbsolute(Index index) {
        return new DecimalPosition(index.scale(TILE_SIZE)).add(absoluteOffset);
    }

    public DecimalPosition toAbsoluteMiddle(Index index) {
        return toAbsolute(index).add(TILE_SIZE / 2.0, TILE_SIZE / 2.0);
    }

    public Index toTile(DecimalPosition absolutePosition) {
        return absolutePosition.sub(absoluteOffset).divide(TILE_SIZE).toIndexFloor();
    }

    public ObstacleContainerTile getObstacleContainerTile(Index index) {
        if (index.getY() >= xCount || index.getX() < 0 || index.getY() >= yCount || index.getY() < 0) {
            return null;
        }
        return obstacleContainerTiles[index.getX()][index.getY()];
    }

    private ObstacleContainerTile getOrCreate(Index index) {
        ObstacleContainerTile obstacleContainerTile = getObstacleContainerTile(index);
        if (obstacleContainerTile == null) {
            obstacleContainerTile = new ObstacleContainerTile();
            obstacleContainerTiles[index.getX()][index.getY()] = obstacleContainerTile;
        }
        return obstacleContainerTile;
    }

    public int getXCount() {
        return xCount;
    }

    public int getYCount() {
        return yCount;
    }

    public boolean hasNorthSuccessorNode(int currentTilePositionY) {
        return currentTilePositionY < yCount - 1;
    }

    public boolean hasEastSuccessorNode(int currentTilePositionX) {
        return currentTilePositionX < xCount - 1;
    }

    public boolean hasSouthSuccessorNode(int currentTilePositionY) {
        return currentTilePositionY > 0;
    }

    public boolean hasWestSuccessorNode(int currentTilePositionX) {
        return currentTilePositionX > 0;
    }

    public boolean hasBlockingTerrain(int tileX, int tileY) {
        return obstacleContainerTiles[tileX][tileY] != null;
    }

    public boolean isInSight(SyncPhysicalArea syncPhysicalArea, DecimalPosition target) {
        if (syncPhysicalArea.getPosition2d().equals(target)) {
            return true;
        }
        double angel = syncPhysicalArea.getPosition2d().getAngle(target);
        double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(syncPhysicalArea.getPosition2d(), target);
        Line line1 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel1, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel1, syncPhysicalArea.getRadius()));
        Line line2 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel2, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel2, syncPhysicalArea.getRadius()));

        return !isSightBlocked(line) && !isSightBlocked(line1) && !isSightBlocked(line2);
    }

    private boolean isSightBlocked(Line line) {
        List<Index> tiles = absoluteLineToTiles(line);
        for (Index tile : tiles) {
            ObstacleContainerTile obstacleContainerTile = getObstacleContainerTile(tile);
            if (obstacleContainerTile != null) {
                for (Obstacle obstacle : obstacleContainerTile.getObstacles()) {
                    if (obstacle.isPiercing(line)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
