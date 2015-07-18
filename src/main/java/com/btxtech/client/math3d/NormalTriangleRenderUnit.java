package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 01.05.2015.
 */
public class NormalTriangleRenderUnit extends AbstractTriangleRenderUnit {
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private WebGLBuffer aVertexNormal;
    private WebGLBuffer textureCoordinateBuffer;
    private int normalPositionAttribute;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;

    public NormalTriangleRenderUnit(WebGLRenderingContext ctx3d, String vertexShaderCode, String fragmentShaderCode) {
        createProgram(ctx3d, vertexShaderCode, fragmentShaderCode);
        aVertexNormal = ctx3d.createBuffer();
        textureCoordinateBuffer = ctx3d.createBuffer();
        normalPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        textureCoordinatePositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    public void createTexture(final WebGLRenderingContext ctx3d, ImageDescriptor imageDescriptor) {
        webGLTexture = setupTexture(ctx3d, imageDescriptor);
    }

    @Override
    protected void fillCustomBuffers(WebGLRenderingContext ctx3d, VertexList vertexList) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", ctx3d);
    }

    @Override
    protected void customDraw(WebGLRenderingContext ctx3d, Lighting lighting) {
        // Set the normals
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", ctx3d);
        ctx3d.vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", ctx3d);
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

        // set vertices texture coordinates
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        ctx3d.vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Texture
        WebGLUniformLocation tUniform = getWebGlProgram().getUniformLocation(SAMPLER_UNIFORM_NAME);
        ctx3d.activeTexture(WebGLRenderingContext.TEXTURE0);
        ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        ctx3d.uniform1i(tUniform, 0);
    }
}
