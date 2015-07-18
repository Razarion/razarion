package com.btxtech.client.math3d;

import com.btxtech.client.terrain.VertexList;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 20.05.2015.
 */
public class LightTriangleRenderUnit extends AbstractTriangleRenderUnit {
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String UNIFORM_COLOR = "uColor";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";

    private Color color;
    private WebGLBuffer aVertexNormal;
    private int normalPositionAttribute;

    public LightTriangleRenderUnit(WebGLRenderingContext ctx3d, String vertexShaderCode, String fragmentShaderCode, Color color) {
        this.color = color;
        createProgram(ctx3d, vertexShaderCode, fragmentShaderCode);
        aVertexNormal = ctx3d.createBuffer();
        normalPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(A_VERTEX_NORMAL);
    }

    @Override
    protected void fillCustomBuffers(WebGLRenderingContext ctx3d, VertexList vertexList) {
        // normal vector
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    @Override
    protected void customDraw(WebGLRenderingContext ctx3d, Lighting lighting) {
        // Set the normals
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", ctx3d);
        // Color uniform
        WebGLUniformLocation pUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_COLOR);
        ctx3d.uniform4f(pUniformColor, (float) color.getR(), (float) color.getG(), (float) color.getB(), (float) color.getA());
        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_AMBIENT_COLOR);
        ctx3d.uniform3f(pAmbientUniformColor, (float) lighting.getAmbientColor().getR(), (float) lighting.getAmbientColor().getG(), (float) lighting.getAmbientColor().getB());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        ctx3d.uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pLightingColorUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_DIRECTIONAL_COLOR);
        ctx3d.uniform3f(pLightingColorUniformColor, (float) lighting.getColor().getR(), (float) lighting.getColor().getG(), (float) lighting.getColor().getB());
    }
}
