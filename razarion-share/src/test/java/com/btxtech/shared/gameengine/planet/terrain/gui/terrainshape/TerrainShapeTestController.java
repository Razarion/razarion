package com.btxtech.shared.gameengine.planet.terrain.gui.terrainshape;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TerrainShapeTestController extends AbstractTerrainTestController {
    private TerrainShape actual;

    public TerrainShapeTestController(TerrainShape actual) {
        this.actual = actual;
    }

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        return new TerrainShapeTestRenderer(actual);
    }
}
