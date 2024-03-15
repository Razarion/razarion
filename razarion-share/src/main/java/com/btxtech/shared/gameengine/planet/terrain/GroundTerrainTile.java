package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import jsinterop.annotations.JsType;

@JsType
public class GroundTerrainTile {
    public int groundConfigId;
    public Float32ArrayEmu positions;
    public Float32ArrayEmu norms;
}
