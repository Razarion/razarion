package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.nativejs.NativeMatrix;
import jsinterop.annotations.JsType;

@JsType
public class TerrainObjectModel {
    @Deprecated
    public NativeMatrix model;
    public int terrainObjectId;
    public Vertex position;
    public Vertex scale;
    public Vertex rotation;
}
