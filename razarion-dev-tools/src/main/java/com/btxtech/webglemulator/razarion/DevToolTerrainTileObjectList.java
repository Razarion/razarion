package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.nativejs.NativeMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 19.01.2018.
 */
public class DevToolTerrainTileObjectList extends TerrainTileObjectList {
    private int terrainObjectConfigId;
    private List<NativeMatrix> nativeMatrices = new ArrayList<>();

    @Override
    public int getTerrainObjectConfigId() {
        return terrainObjectConfigId;
    }

    @Override
    public void setTerrainObjectConfigId(int terrainObjectConfigId) {
        this.terrainObjectConfigId = terrainObjectConfigId;
    }

    @Override
    public void addModel(NativeMatrix newMatrix) {
        nativeMatrices.add(newMatrix);
    }

    @Override
    public NativeMatrix[] getModels() {
        return nativeMatrices.toArray(new NativeMatrix[nativeMatrices.size()]);
    }
}
