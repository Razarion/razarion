package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import com.btxtech.shared.datatypes.Vertex;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeVertex", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeVertex {
    public double x;
    public double y;
    public double z;

    public static NativeVertex fromVertex(Vertex vertex) {
        NativeVertex nativeVertex = new NativeVertex();
        nativeVertex.x = vertex.getX();
        nativeVertex.y = vertex.getY();
        nativeVertex.z = vertex.getZ();
        return nativeVertex;
    }
}
