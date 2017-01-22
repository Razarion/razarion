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
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.utils.GeometricUtil;

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
        absoluteOffset = new DecimalPosition(groundMeshDimension.getStart()).multiply(TerrainService.MESH_NODE_EDGE_LENGTH);
        xCount = (int) Math.ceil(groundMeshDimension.width() * TerrainService.MESH_NODE_EDGE_LENGTH / TILE_SIZE);
        yCount = (int) Math.ceil(groundMeshDimension.height() * TerrainService.MESH_NODE_EDGE_LENGTH / TILE_SIZE);
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
        List<Index> tiles = GeometricUtil.rasterizeLine(offset(obstacleLine.getLine()), TILE_SIZE);
        for (Index tile : tiles) {
            getOrCreate(tile).addObstacle(obstacleLine);
        }
    }

    private void insertObstacleCircle(ObstacleCircle obstacleCircle) {
        List<Index> tiles = GeometricUtil.rasterizeCircle(offset(obstacleCircle.getCircle()), TILE_SIZE);
        for (Index tile : tiles) {
            getOrCreate(tile).addObstacle(obstacleCircle);
        }
    }

    public Iterable<Obstacle> getObstacles(SyncPhysicalArea syncPhysicalArea) {
        List<Index> tiles = GeometricUtil.rasterizeCircle(offset(new Circle2D(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius())), TILE_SIZE);
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
        return new DecimalPosition(index.scale(TILE_SIZE)).sub(absoluteOffset);
    }


    private Circle2D offset(Circle2D circle) {
        return circle.translate(absoluteOffset);
    }

    private Line offset(Line line) {
        return line.translate(absoluteOffset);
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
}
