package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import elemental.html.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 22.12.2015.
 */
public class IntegerShaderAttribute extends AbstractShaderAttribute {

    public IntegerShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 1);
    }

    public void fillBuffer(List<Integer> integers) {
        List<Double> doubleList = new ArrayList<>();
        for (Integer integer : integers) {
            doubleList.add(integer.doubleValue());
        }
        fillDoubleBuffer(doubleList);
    }

}