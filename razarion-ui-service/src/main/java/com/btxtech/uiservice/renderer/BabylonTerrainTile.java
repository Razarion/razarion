package com.btxtech.uiservice.renderer;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BabylonTerrainTile {

    void addToScene();

    void removeFromScene();
}
