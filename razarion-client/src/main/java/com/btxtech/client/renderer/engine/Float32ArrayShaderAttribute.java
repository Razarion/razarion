package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.ClientRenderUtil;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.shared.datatypes.shape.Float32ArrayEmu;
import elemental.html.WebGLRenderingContext;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class Float32ArrayShaderAttribute extends AbstractShaderAttribute {

    public Float32ArrayShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 1);
    }

    public void fillFloat32ArrayEmu(Float32ArrayEmu float32Array) {
        fillFloat32Array(ClientRenderUtil.toFloat32Array(float32Array));
    }
}
