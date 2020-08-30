package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgramFacade;
import elemental2.webgl.WebGLRenderingContext;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class Vec2Float32ArrayShaderAttribute extends AbstractShaderAttribute {

    public Vec2Float32ArrayShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgramFacade webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 2);
    }
}
