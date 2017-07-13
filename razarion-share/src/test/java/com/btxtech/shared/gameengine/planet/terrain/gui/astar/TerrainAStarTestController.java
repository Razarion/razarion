package com.btxtech.shared.gameengine.planet.terrain.gui.astar;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.pathing.AStar;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TerrainAStarTestController extends AbstractTerrainTestController {
    private TerrainShape actual;
    private SimplePath simplePath;
    private AStar aStar;

    public TerrainAStarTestController(TerrainShape actual, SimplePath simplePath, AStar aStar) {
        this.actual = actual;
        this.simplePath = simplePath;
        this.aStar = aStar;
    }

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        return new TerrainAStarTestRenderer(actual, simplePath, aStar);
    }
}
