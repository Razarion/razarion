package com.btxtech.shared.gameengine.planet.terrain.container.json;


import jsinterop.annotations.JsType;

@JsType(name = "NativeTerrainShapeTile", namespace = "com.btxtech.shared.json")
public class NativeTerrainShapeTile {
    public NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;
    public NativeBabylonDecal[] nativeBabylonDecals;
    public NativeBotGround[] nativeBotGrounds;
}
