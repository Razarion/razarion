package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
public abstract class AbstractTerrainSurfaceDepthBufferRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer barycentricBuffer;
    private int barycentricPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainDepthBufferObjectRenderer.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    @Inject
    private Shadowing shadowing;

    protected abstract VertexList provideVertexList();

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        barycentricBuffer = gameCanvas.getCtx3d().createBuffer();
        barycentricPositionAttribute = getAndEnableAttributeLocation(BARYCENTRIC_ATTRIBUTE_NAME);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
        webGLTexture = setupTexture(CHESS_TEXTURE_08);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = provideVertexList();
        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // barycentric
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createBarycentricDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // texture Coordinate
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        // Projection uniform
        WebGLUniformLocation perspectiveUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveUniform, false, WebGlUtil.createArrayBufferOfFloat32(shadowing.createProjectionTransformation().toWebGlArray()));
        // View transformation uniform
        WebGLUniformLocation viewUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(viewUniform, false, WebGlUtil.createArrayBufferOfFloat32(shadowing.createViewTransformation().toWebGlArray()));
        // Model transformation uniform
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32(Matrix4.createIdentity().toWebGlArray()));
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set the barycentric coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(barycentricPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Texture
        WebGLUniformLocation tUniform = getUniformLocation(SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().uniform1i(tUniform, 0);
        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
