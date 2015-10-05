package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.Shadowing;
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
    private static final String EDGE_POSITION_ATTRIBUTE_NAME = "aEdgePosition";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String TOP_SAMPLER_UNIFORM_NAME = "uSamplerTop";
    private static final String BLEND_SAMPLER_UNIFORM_NAME = "uSamplerBlend";
    private static final String BOTTOM_SAMPLER_UNIFORM_NAME = "uSamplerBottom";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private static final String UNIFORM_EDGE_DISTANCE = "uEdgeDistance";
    private static final String UNIFORM_MVP_SHADOW_BIAS = "uMVPDepthBias";
    private static final String UNIFORM_SHADOW_MAP_SAMPLER = "uSamplerShadow";
    private static final String UNIFORM_SHADOW_ALPHA = "uShadowAlpha";

    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer normalBuffer;
    private int normalPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private int edgePositionAttribute;
    private WebGLBuffer edgeBuffer;
    private WebGLTexture topWebGLTexture;
    private WebGLTexture blendWebGLTexture;
    private WebGLTexture bottomWebGLTexture;
    private int elementCount;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Lighting lighting;
    @Inject
    @Normal
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private RenderService renderService;
    @Inject
    private Shadowing shadowing;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainSurfaceVertexShader(), Shaders.INSTANCE.terrainSurfaceFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        normalBuffer = gameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        edgeBuffer = gameCanvas.getCtx3d().createBuffer();
        edgePositionAttribute = getAndEnableAttributeLocation(EDGE_POSITION_ATTRIBUTE_NAME);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);

        topWebGLTexture = setupTexture(terrainSurface.getTopImageDescriptor());
        blendWebGLTexture = setupTexture(terrainSurface.getBlendImageDescriptor());
        bottomWebGLTexture = setupTexture(terrainSurface.getBottomImageDescriptor());
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

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, edgeBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createEdgeDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gameCanvas.getCtx3d());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        // Projection uniform
        WebGLUniformLocation pUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(camera.createMatrix().toWebGlArray()));

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
        // Edges
        WebGLUniformLocation edgeDistanceUniform = getUniformLocation(UNIFORM_EDGE_DISTANCE);
        gameCanvas.getCtx3d().uniform1f(edgeDistanceUniform, (float) terrainSurface.getEdgeDistance());

        // Shadow
        WebGLUniformLocation shadowMvpUniform = getUniformLocation(UNIFORM_MVP_SHADOW_BIAS);
        gameCanvas.getCtx3d().uniformMatrix4fv(shadowMvpUniform, false, WebGlUtil.createArrayBufferOfFloat32(shadowing.createViewProjectionTransformation().toWebGlArray()));
        WebGLUniformLocation shadowMapUniform = getUniformLocation(UNIFORM_SHADOW_MAP_SAMPLER);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE4);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        gameCanvas.getCtx3d().uniform1i(shadowMapUniform, 4);
        WebGLUniformLocation uniformShadowAlpha = getUniformLocation(UNIFORM_SHADOW_ALPHA);
        gameCanvas.getCtx3d().uniform1f(uniformShadowAlpha, (float) shadowing.getShadowAlpha());

        // Positions
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Set the normals
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Edges
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, edgeBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(edgePositionAttribute, 1, WebGLRenderingContext.FLOAT, false, 0, 0);
        // Textures
        WebGLUniformLocation tTopUniform = getUniformLocation(TOP_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, topWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tTopUniform, 0);

        WebGLUniformLocation tBlendUniform = getUniformLocation(BLEND_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE1);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, blendWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tBlendUniform, 1);

        WebGLUniformLocation tBottomUniform = getUniformLocation(BOTTOM_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE2);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, bottomWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tBottomUniform, 2);
        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
