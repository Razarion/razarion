package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Created by Beat
 * 01.04.2017.
 */
@ApplicationScoped
public class TestJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainNode generateTerrainNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainSubNode generateTerrainSubNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainTileObjectList generateTerrainTileObjectList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Float32ArrayEmu newFloat32Array(List<Vertex> vertices) {
        throw new UnsupportedOperationException();
    }
}
