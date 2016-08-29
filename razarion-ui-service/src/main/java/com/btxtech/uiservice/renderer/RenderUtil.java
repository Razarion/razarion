package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 28.08.2016.
 */
public interface RenderUtil {

    static List<Double> setupNormDoubles(List<Vertex> vertices, List<Vertex> norms) {
        List<Double> normDoubles = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            Vertex norm = norms.get(i).normalize(0.1);
            vertex.appendTo(normDoubles);
            vertex.add(norm).appendTo(normDoubles);
        }
        return normDoubles;
    }


}
