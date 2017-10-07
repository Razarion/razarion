package com.btxtech.shared.gameengine.planet.terrain.container;

import org.junit.Test;

/**
 * Created by Beat
 * on 05.10.2017.
 */
public class TerrainShapeSubNodeFactoryTest {
    @Test
    public void createTerrainShapeSubNode() throws Exception {
        TerrainShapeSubNodeFactory terrainShapeSubNodeFactory = new TerrainShapeSubNodeFactory();
        terrainShapeSubNodeFactory.fillTerrainShapeSubNode(null, null, null, null, null);
    }

}