package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import elemental.html.WebGLRenderingContext;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class Vec2Float32ArrayShaderAttribute extends AbstractShaderAttribute {

    public Vec2Float32ArrayShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 2);
    }
}
