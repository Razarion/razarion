package com.btxtech.client.math3d;

import com.btxtech.client.GameCanvas;
import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent // Why is dependent needed???
public class WireTriangleRenderUnit extends AbstractTriangleRenderUnit {
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private WebGLBuffer barycentricBuffer;
    private int barycentricPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;
    @Inject
    private GameCanvas gameCanvas;

    public void init(String vertexShaderCode, String fragmentShaderCode) {
        gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        createProgram(vertexShaderCode, fragmentShaderCode);
        barycentricBuffer = gameCanvas.getCtx3d().createBuffer();
        barycentricPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(BARYCENTRIC_ATTRIBUTE_NAME);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    public void createTexture(ImageDescriptor imageDescriptor) {
        webGLTexture = setupTexture(imageDescriptor);
    }

    @Override
    protected void fillCustomBuffers(VertexList vertexList) {
        // barycentric
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createBarycentricDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // texture Coordinate
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);
    }

    @Override
    protected void customDraw() {
        // set the barycentric coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(barycentricPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Texture
        WebGLUniformLocation tUniform = getWebGlProgram().getUniformLocation(SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().uniform1i(tUniform, 0);
    }
}
