package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.shared.datatypes.TextureCoordinate;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class ShaderTextureCoordinateAttribute extends AbstractShaderAttribute {
    private int attributeLocation;
    private WebGLBuffer webGlBuffer;

    public ShaderTextureCoordinateAttribute(WebGLRenderingContext ctx3d, WebGlProgram webGlProgram, String attributeName) {
        super(ctx3d, webGlProgram, attributeName, TextureCoordinate.getComponentCount());
    }

    public void fillBuffer(List<TextureCoordinate> textureCoordinates) {
        List<Double> doubleList = new ArrayList<>();
        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            textureCoordinate.appendTo(doubleList);
        }
        fillDoubleBuffer(doubleList);
    }
}
