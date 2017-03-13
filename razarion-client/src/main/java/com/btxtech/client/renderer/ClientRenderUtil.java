package com.btxtech.client.renderer;

import com.btxtech.shared.datatypes.shape.Float32ArrayEmu;
import elemental.html.Float32Array;

import java.util.List;

/**
 * Created by Beat
 * 07.03.2017.
 */
public class ClientRenderUtil {
    public static Float32Array setupNormFloat32Array(Float32Array vertexFloat32Array, Float32Array normFloat32Array) {
        // TODO fix
        // TODO fix also DevToolRenderUtil
        throw new UnsupportedOperationException();
//        for(int i = 0; i < vertexFloat32Array.length(); i += 3) {
//            vertexFloat32Array.numberAt(i)
//        }
    }

    public static List<Double> setupNormFloat32Array(Float32ArrayEmu vertices, Float32ArrayEmu norms) {
        // TODO fix
        return null;
    }


    public static native Float32Array toFloat32Array(Float32ArrayEmu float32Array) /*-{
        return float32Array;
    }-*/;

}
