package com.btxtech.shared.gameengine.planet.terrain.gui.astar;

import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TerrainAStarTestController extends AbstractTerrainTestController {
    private DisplayDTO displayDTO;

    public TerrainAStarTestController(DisplayDTO displayDTO) {
        this.displayDTO = displayDTO;
    }

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        return new TerrainAStarTestRenderer(displayDTO);
    }
}
