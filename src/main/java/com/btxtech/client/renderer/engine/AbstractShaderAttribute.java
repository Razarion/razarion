package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;

import java.util.List;

/**
 * Created by Beat
 * 19.12.2015.
 */
abstract public class AbstractShaderAttribute {
    private WebGLRenderingContext ctx3d;
    private final int size;
    private int attributeLocation;
    private WebGLBuffer webGlBuffer;

    protected AbstractShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName, int size) {
        this.ctx3d = ctx3d;
        this.size = size;
        attributeLocation = webGlProgram.getAndEnableAttributeLocation(attributeName);
        webGlBuffer = ctx3d.createBuffer();
        WebGlUtil.checkLastWebGlError("createBuffer", ctx3d);
    }

    protected void fillDoubleBuffer(List<Double> doubleList) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(doubleList), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    protected void activate() {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.vertexAttribPointer(attributeLocation, size, WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", ctx3d);
    }
}
