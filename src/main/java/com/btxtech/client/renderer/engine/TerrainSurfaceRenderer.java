package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ModelTransformation;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
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
 * 01.05.2015.
 */
@Dependent
public class TerrainSurfaceRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String MODEL_VIEW_UNIFORM_NAME = "uMVMatrix";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String TEXTURE_SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer normalBuffer;
    private int normalPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;
    private int elementCount;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Lighting lighting;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private ModelTransformation modelTransformation;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainSurfaceVertexShader(), Shaders.INSTANCE.terrainSurfaceFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        normalBuffer = gameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);

        webGLTexture = setupTexture(terrainSurface.getImageDescriptor());
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gameCanvas.getCtx3d());

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gameCanvas.getCtx3d());

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gameCanvas.getCtx3d());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();

        // Projection uniform
        WebGLUniformLocation pUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = getUniformLocation(MODEL_VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(viewTransformation.createMatrix().multiply(modelTransformation.createMatrix()).toWebGlArray()));

        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getUniformLocation(UNIFORM_AMBIENT_COLOR);
        gameCanvas.getCtx3d().uniform3f(pAmbientUniformColor, (float) lighting.getAmbientColor().getR(), (float) lighting.getAmbientColor().getG(), (float) lighting.getAmbientColor().getB());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        gameCanvas.getCtx3d().uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pLightingColorUniformColor = getUniformLocation(UNIFORM_DIRECTIONAL_COLOR);
        gameCanvas.getCtx3d().uniform3f(pLightingColorUniformColor, (float) lighting.getColor().getR(), (float) lighting.getColor().getG(), (float) lighting.getColor().getB());

        // Positions
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Set the normals
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Textures
        WebGLUniformLocation textureUniform = getUniformLocation(TEXTURE_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().uniform1i(textureUniform, 0);
        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
