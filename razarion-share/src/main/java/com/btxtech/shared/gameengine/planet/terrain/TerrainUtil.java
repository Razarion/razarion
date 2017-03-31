package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;

/**
 * Created by Beat
 * 28.03.2017.
 */
public interface TerrainUtil {
    int GROUND_NODE_ABSOLUTE_LENGTH = 8;
    int TERRAIN_TILE_NODES_COUNT = 20;
    double TERRAIN_TILE_ABSOLUTE_LENGTH = GROUND_NODE_ABSOLUTE_LENGTH * TERRAIN_TILE_NODES_COUNT;

    static Index toTile(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexFloor();
    }


    static DecimalPosition toAbsolute(Index tile) {
        return new DecimalPosition(tile.scale(TERRAIN_TILE_ABSOLUTE_LENGTH));
    }

    static Rectangle2D toAbsoluteRectangle(Index tile) {
        DecimalPosition start = toAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_TILE_ABSOLUTE_LENGTH, TERRAIN_TILE_ABSOLUTE_LENGTH);
    }
}
