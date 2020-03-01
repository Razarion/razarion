package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import elemental2.webgl.WebGLRenderingContext;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class Float32ArrayShaderAttribute extends AbstractShaderAttribute {

    public Float32ArrayShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 1);
    }
}
