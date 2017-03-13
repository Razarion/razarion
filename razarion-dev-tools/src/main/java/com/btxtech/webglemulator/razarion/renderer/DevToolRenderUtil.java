package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Float32ArrayEmu;

import java.util.List;

/**
 * Created by Beat
 * 08.03.2017.
 */
public interface DevToolRenderUtil {
    static List<Double> setupNormDoubles(List<Double> vertices, List<Double> norms) {
        // TODO fix
        // TODO fix also ClientRenderUtil
        throw new UnsupportedOperationException();
    }

    static List<Double> toDoubles(Float32ArrayEmu vertices) {
        throw new UnsupportedOperationException();
    }

    static List<Double> setupNormDoubles(Float32ArrayEmu vertices, Float32ArrayEmu norms) {
        // TODO fix
        // TODO fix also ClientRenderUtil
        throw new UnsupportedOperationException();
    }

//    public interface RenderUtil {
//
//        static List<Double> setupNormDoubles(List<Vertex> vertices, List<Vertex> norms) {
//            List<Double> normDoubles = new ArrayList<>();
//            for (int i = 0; i < vertices.size(); i++) {
//                Vertex vertex = vertices.get(i);
//                Vertex norm = norms.get(i).normalize(0.1);
//                vertex.appendTo(normDoubles);
//                vertex.add(norm).appendTo(normDoubles);
//            }
//            return normDoubles;
//        }
//
//
//    }

}
