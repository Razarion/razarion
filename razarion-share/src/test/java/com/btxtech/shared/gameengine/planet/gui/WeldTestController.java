package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 09.04.2017.
 */
@Singleton
public class WeldTestController extends AbstractTerrainTestController {
    @Inject
    private WeldTestRenderer weldTestRenderer;
    @Inject
    private TerrainService terrainService;
    private Object[] userObjects;

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        weldTestRenderer.setupFields(userObjects);
        return weldTestRenderer;
    }

    public void setUserObjects(Object[] userObjects) {
        this.userObjects = userObjects;
    }

    protected void onMousePressedTerrain(DecimalPosition position) {
        System.out.println("-----------------------------------------------");
        TerrainShapeNode terrainShapeNode = terrainService.getPathingAccess().getTerrainShapeNode(TerrainUtil.toNode(position));
        if(terrainShapeNode == null) {
            System.out.println("No terrain shape node at: " + position);
            return;
        }
        System.out.println("Terrain shape node at: " + position);
        System.out.println("Height: " + terrainShapeNode.getUniformGroundHeight());

    }
}
