package com.btxtech.shared.gameengine.planet.terrain.gui.astar;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.pathing.AStar;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;

/**
 * Created by Beat
 * on 28.09.2017.
 */
public class DisplayDTO {
    private TerrainShape terrainShape;
    private SimplePath simplePath;
    private AStar aStar;
    private PathingNodeWrapper pathingNodeWrapper;

    public TerrainShape getTerrainShape() {
        return terrainShape;
    }

    public void setTerrainShape(TerrainShape terrainShape) {
        this.terrainShape = terrainShape;
    }

    public SimplePath getSimplePath() {
        return simplePath;
    }

    public void setSimplePath(SimplePath simplePath) {
        this.simplePath = simplePath;
    }

    public AStar getaStar() {
        return aStar;
    }

    public void setaStar(AStar aStar) {
        this.aStar = aStar;
    }

    public PathingNodeWrapper getPathingNodeWrapper() {
        return pathingNodeWrapper;
    }

    public void setPathingNodeWrapper(PathingNodeWrapper pathingNodeWrapper) {
        this.pathingNodeWrapper = pathingNodeWrapper;
    }
}
