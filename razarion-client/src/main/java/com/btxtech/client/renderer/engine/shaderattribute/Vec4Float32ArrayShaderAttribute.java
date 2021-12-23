package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgramFacade;
import elemental2.webgl.WebGLRenderingContext;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class Vec4Float32ArrayShaderAttribute extends AbstractShaderAttribute {

    public Vec4Float32ArrayShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgramFacade webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 4);
    }
}
