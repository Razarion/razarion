package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.nativejs.NativeMatrix;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 18.01.2018.
 */
@JsType(isNative = true, name = "TerrainTileObjectList", namespace = "com.btxtech.shared.nativejs")
public class TerrainTileObjectList {
    public native int getTerrainObjectConfigId();

    public native void setTerrainObjectConfigId(int terrainObjectConfigId);

    public native void addModel(NativeMatrix newMatrix);

    public native NativeMatrix[] getModels();
}
