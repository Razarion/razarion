package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class VertexShaderAttribute extends AbstractShaderAttribute {

    public VertexShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, Vertex.getComponentsPerVertex());
    }

    public void fillBuffer(List<Vertex> vertices) {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : vertices) {
            vertex.appendTo(doubleList);
        }
        fillDoubleBuffer(doubleList);
    }

    @Override
    public String toString() {
        return "VertexShaderAttribute{" +
                super.toString() +
                "}";
    }
}
