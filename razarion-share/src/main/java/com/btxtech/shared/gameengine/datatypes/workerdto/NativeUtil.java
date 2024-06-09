package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.nativejs.NativeVertexDto;

/**
 * Created by Beat
 * on 12.01.2018.
 */
public interface NativeUtil {
    static Vertex toVertex(NativeVertexDto nativeVertexDto) {
        if (nativeVertexDto != null) {
            return new Vertex(nativeVertexDto.x, nativeVertexDto.y, nativeVertexDto.z);
        } else {
            return null;
        }
    }

    static NativeVertexDto toNativeVertex(Vertex vertex) {
        NativeVertexDto nativeVertexDto = new NativeVertexDto();
        nativeVertexDto.x = vertex.getX();
        nativeVertexDto.y = vertex.getY();
        nativeVertexDto.z = vertex.getZ();
        return nativeVertexDto;
    }

    static NativeVertexDto normalize(NativeVertexDto vertex) {
        double magnitude = Math.sqrt(vertex.x * vertex.x + vertex.y * vertex.y + vertex.z * vertex.z);
        NativeVertexDto nativeVertexDto = new NativeVertexDto();
        nativeVertexDto.x = vertex.x / magnitude;
        nativeVertexDto.y = vertex.y / magnitude;
        nativeVertexDto.z = vertex.z / magnitude;
        return nativeVertexDto;
    }

    static double angleXY(NativeVertexDto direction) {
        return Math.atan2(direction.y, direction.x);
    }

    static DecimalPosition toSyncBaseItemPosition2d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        } else {
            return new DecimalPosition(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y);
        }
    }
}
