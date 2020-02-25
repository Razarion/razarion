package com.btxtech.client.renderer.engine.shaderattribute;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.shared.datatypes.DecimalPosition;
import elemental2.webgl.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 05.02.2017.
 */
public class DecimalPositionShaderAttribute extends AbstractShaderAttribute {
    public DecimalPositionShaderAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, 2);
    }

    public void fillBuffer(List<DecimalPosition> decimalPositions) {
        List<Double> doubleList = new ArrayList<>();
        for (DecimalPosition decimalPosition : decimalPositions) {
            doubleList.add(decimalPosition.getX());
            doubleList.add(decimalPosition.getY());
        }
        fillDoubleBuffer(doubleList);
    }

}
