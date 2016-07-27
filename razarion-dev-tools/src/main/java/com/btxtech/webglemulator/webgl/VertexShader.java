package com.btxtech.webglemulator.webgl;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;

/**
 * Created by Beat
 * 22.05.2016.
 */
public interface VertexShader {
    Vertex4 runShader(Vertex vertex);
}
