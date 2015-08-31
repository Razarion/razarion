package com.btxtech.client.math3d;

import com.btxtech.client.GameCanvas;
import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.shaders.Shaders;
import com.btxtech.client.terrain.VertexList;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@Dependent // Why is dependent needed???
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
    @Inject
    private GameCanvas gagameCanvas;
    @Inject
    private Lighting lighting;

    public void init(ImageDescriptor imageDescriptor) {
        createProgram(Shaders.INSTANCE.normalVertexShader(), Shaders.INSTANCE.normalFragmentShader());
        aVertexNormal = gagameCanvas.getCtx3d().createBuffer();
        textureCoordinateBuffer = gagameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        textureCoordinatePositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
        webGLTexture = setupTexture(imageDescriptor);
    }

    @Override
    protected void fillCustomBuffers(VertexList vertexList) {
        gagameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", gagameCanvas.getCtx3d());
        gagameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gagameCanvas.getCtx3d());
        gagameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gagameCanvas.getCtx3d());
        gagameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gagameCanvas.getCtx3d());
    }

    @Override
    protected void customDraw() {
        // Set the normals
        gagameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", gagameCanvas.getCtx3d());
        gagameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", gagameCanvas.getCtx3d());
        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_AMBIENT_COLOR);
        gagameCanvas.getCtx3d().uniform3f(pAmbientUniformColor, (float) lighting.getAmbientColor().getR(), (float) lighting.getAmbientColor().getG(), (float) lighting.getAmbientColor().getB());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        gagameCanvas.getCtx3d().uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pLightingColorUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_DIRECTIONAL_COLOR);
        gagameCanvas.getCtx3d().uniform3f(pLightingColorUniformColor, (float) lighting.getColor().getR(), (float) lighting.getColor().getG(), (float) lighting.getColor().getB());

        // set vertices texture coordinates
        gagameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gagameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Texture
        WebGLUniformLocation tUniform = getWebGlProgram().getUniformLocation(SAMPLER_UNIFORM_NAME);
        gagameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gagameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gagameCanvas.getCtx3d().uniform1i(tUniform, 0);
    }
}
