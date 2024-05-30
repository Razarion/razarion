package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * on 20.06.2017.
 */
public interface TerrainImpactCallback<T> {
    /**
     * Called if no tile for terrain position
     *
     * @param tileIndex the index of the tile. Eg: 0,0 is the tile on bottom left
     * @return result
     */
    default T landNoTile(Index tileIndex) {
        return null;
    }
}
