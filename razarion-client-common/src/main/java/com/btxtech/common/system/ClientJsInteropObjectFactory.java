package com.btxtech.common.system;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;
import elemental2.core.Float32Array;
import jsinterop.base.Js;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Created by Beat
 * 01.04.2017.
 */
@ApplicationScoped
public class ClientJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        return new TerrainSlopeTile() {
        };
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        return new TerrainWaterTile();
    }

    @Override
    public TerrainNode generateTerrainNode() {
        return new TerrainNode() {
        };
    }

    @Override
    public TerrainSubNode generateTerrainSubNode() {
        return new TerrainSubNode() {
        };
    }

    @Override
    public TerrainTileObjectList generateTerrainTileObjectList() {
        return new TerrainTileObjectList() {
        };
    }

    @Override
    public Float32ArrayEmu newFloat32Array(List<Vertex> vertices) {
        return Js.uncheckedCast(new Float32Array(Vertex.toArray(vertices)));
    }
}
