package com.btxtech.renderer.emulation.webgl;

import com.btxtech.shared.primitives.Vertex;
import com.btxtech.shared.primitives.Vertex4;

/**
 * Created by Beat
 * 22.05.2016.
 */
public interface VertexShader {
    Vertex4 process(Vertex vertex);
}
