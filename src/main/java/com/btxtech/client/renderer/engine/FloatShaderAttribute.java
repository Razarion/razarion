package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 22.12.2015.
 */
public class FloatShaderAttribute extends AbstractShaderAttribute {

    protected FloatShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 1);
    }

    public void fillBuffer(List<Double> doubles) {
        fillDoubleBuffer(doubles);
    }

}
