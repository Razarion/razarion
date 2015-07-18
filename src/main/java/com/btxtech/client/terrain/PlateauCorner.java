package com.btxtech.client.terrain;

import com.btxtech.client.math3d.Vertex;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygon;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonCorner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2015.
 */
public class PlateauCorner extends TerrainPolygonCorner {

    public PlateauCorner(TerrainPolygon terrainPolygon, int index, Index point) {
        super(terrainPolygon, index, point);
    }
}
