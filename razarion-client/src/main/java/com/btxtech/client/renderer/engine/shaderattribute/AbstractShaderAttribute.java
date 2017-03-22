package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import elemental.html.Float32Array;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;

import java.util.List;

/**
 * Created by Beat
 * 19.12.2015.
 */
abstract public class AbstractShaderAttribute {
    // private Logger logger = Logger.getLogger(AbstractShaderAttribute.class.getName());
    private WebGLRenderingContext ctx3d;
    private final int size;
    private int attributeLocation;
    private WebGLBuffer webGlBuffer;

    protected AbstractShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName, int size) {
        this.ctx3d = ctx3d;
        this.size = size;
        attributeLocation = webGlProgram.getAttributeLocation(attributeName);
        webGlBuffer = ctx3d.createBuffer();
        WebGlUtil.checkLastWebGlError("createBuffer", ctx3d);
    }

    public void fillFloat32Array(Float32Array float32Array) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, float32Array, WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    public void fillDoubleBuffer(List<Double> doubleList) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32Doubles(doubleList), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    public void fillFloatBuffer(List<Float> floatList) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(floatList), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    public void activate() {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, webGlBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.vertexAttribPointer(attributeLocation, size, WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", ctx3d);
        ctx3d.enableVertexAttribArray(attributeLocation);
        WebGlUtil.checkLastWebGlError("enableVertexAttribArray", ctx3d);
    }

    @Override
    public String toString() {
        return "AbstractShaderAttribute{" +
                "ctx3d=" + ctx3d +
                ", size=" + size +
                ", attributeLocation=" + attributeLocation +
                ", webGlBuffer=" + webGlBuffer +
                '}';
    }
}
