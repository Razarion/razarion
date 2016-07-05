package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import elemental.html.WebGLRenderingContext;

/**
 * Created by Beat
 * 22.12.2015.
 */
public class FloatShaderAttribute extends AbstractShaderAttribute {

    protected FloatShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 1);
    }

}
