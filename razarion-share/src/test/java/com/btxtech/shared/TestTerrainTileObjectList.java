package com.btxtech.shared;

import com.btxtech.shared.cdimock.TestNativeMatrixFactory;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 19.01.2018.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TestTerrainTileObjectList extends TerrainTileObjectList {
    private int terrainObjectConfigId;
    private List<double[]> nativeMatrices;

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
        if (nativeMatrices == null) {
            nativeMatrices = new ArrayList<>();
        }
        nativeMatrices.add(newMatrix.toColumnMajorArray());
    }

    @Override
    public NativeMatrix[] getModels() {
        if (nativeMatrices == null) {
            return null;
        }
        NativeMatrix[] nativeMatrices = new NativeMatrix[this.nativeMatrices.size()];
        for (int i = 0; i < this.nativeMatrices.size(); i++) {
            nativeMatrices[i] = new TestNativeMatrixFactory.TestNativeMatrix(Matrix4.fromColumnMajorOrder(this.nativeMatrices.get(i)));
        }

        return nativeMatrices;
    }
}
