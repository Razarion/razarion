package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 20.05.2015.
 */
public class WireTriangleRenderUnit extends AbstractTriangleRenderUnit {
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private WebGLBuffer barycentricBuffer;
    private int barycentricPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;

    public WireTriangleRenderUnit(WebGLRenderingContext ctx3d, String vertexShaderCode, String fragmentShaderCode) {
        ctx3d.getExtension("OES_standard_derivatives");
        createProgram(ctx3d, vertexShaderCode, fragmentShaderCode);
        barycentricBuffer = ctx3d.createBuffer();
        barycentricPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(BARYCENTRIC_ATTRIBUTE_NAME);
        textureCoordinateBuffer = ctx3d.createBuffer();
        textureCoordinatePositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    public void createTexture(final WebGLRenderingContext ctx3d, ImageDescriptor imageDescriptor) {
        webGLTexture = setupTexture(ctx3d, imageDescriptor);
    }

    @Override
    protected void fillCustomBuffers(WebGLRenderingContext ctx3d, VertexList vertexList) {
        // barycentric
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createBarycentricDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // texture Coordinate
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);
    }

    @Override
    protected void customDraw(WebGLRenderingContext ctx3d, Lighting lighting) {
        // set the barycentric coordinates
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        ctx3d.vertexAttribPointer(barycentricPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
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
