package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;

/**
 * Created by Beat
 * on 18.06.2017.
 */
public class TerrainShapeTile {
    private NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;
    private NativeBabylonDecal[] nativeBabylonDecals;

    public NativeTerrainShapeTile toNativeTerrainShapeTile() {
        NativeTerrainShapeTile nativeTerrainShapeTile = new NativeTerrainShapeTile();
        nativeTerrainShapeTile.nativeTerrainShapeObjectLists = nativeTerrainShapeObjectLists;
        nativeTerrainShapeTile.nativeBabylonDecals = nativeBabylonDecals;
        return nativeTerrainShapeTile;
    }

    public NativeTerrainShapeObjectList[] getNativeTerrainShapeObjectLists() {
        return nativeTerrainShapeObjectLists;
    }

    public void setNativeTerrainShapeObjectLists(NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists) {
        this.nativeTerrainShapeObjectLists = nativeTerrainShapeObjectLists;
    }

    public NativeBabylonDecal[] getNativeBabylonDecals() {
        return nativeBabylonDecals;
    }

    public void setNativeBabylonDecals(NativeBabylonDecal[] nativeBabylonDecals) {
        this.nativeBabylonDecals = nativeBabylonDecals;
    }
}
