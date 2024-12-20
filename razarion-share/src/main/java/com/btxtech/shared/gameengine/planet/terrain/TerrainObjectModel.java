package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Vertex;
import jsinterop.annotations.JsType;

@JsType
public class TerrainObjectModel {
    public int terrainObjectId;
    public Vertex position;
    public Vertex scale;
    public Vertex rotation;
}
