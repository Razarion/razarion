package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * on 28.06.2017.
 */
public interface NativeHelper {
    static NativeVertex fromVertex(Vertex vertex) {
        NativeVertex nativeVertex = new NativeVertex();
        nativeVertex.x = vertex.getX();
        nativeVertex.y = vertex.getY();
        nativeVertex.z = vertex.getZ();
        return nativeVertex;
    }
}
