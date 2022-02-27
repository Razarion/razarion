package com.btxtech.uiservice.renderer;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsTerrainTile {

    void addToScene();

    void removeFromScene();
}
