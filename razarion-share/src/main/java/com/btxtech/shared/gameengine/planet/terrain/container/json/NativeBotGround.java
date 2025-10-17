package com.btxtech.shared.gameengine.planet.terrain.container.json;

import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import jsinterop.annotations.JsType;

@JsType(name = "NativeBotGround", namespace = "com.btxtech.shared.json")
public class NativeBotGround {
    public int model3DId;
    public double height;
    public NativeDecimalPosition[] positions;
    public NativeBotGroundSlopeBox[] botGroundSlopeBoxes;
}
