package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGround;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;

public class TerrainShapeTile {
    private NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;
    private NativeBabylonDecal[] nativeBabylonDecals;
    private NativeBotGround[] nativeBotGrounds;

    public NativeTerrainShapeTile toNativeTerrainShapeTile() {
        NativeTerrainShapeTile nativeTerrainShapeTile = new NativeTerrainShapeTile();
        nativeTerrainShapeTile.nativeTerrainShapeObjectLists = nativeTerrainShapeObjectLists;
        nativeTerrainShapeTile.nativeBabylonDecals = nativeBabylonDecals;
        nativeTerrainShapeTile.nativeBotGrounds = nativeBotGrounds;
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

    public NativeBotGround[] getNativeBotGrounds() {
        return nativeBotGrounds;
    }

    public void setNativeBotGrounds(NativeBotGround[] nativeBotGrounds) {
        this.nativeBotGrounds = nativeBotGrounds;
    }
}
