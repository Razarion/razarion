package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
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
    private static final String A_VERTEX_TANGENT = "aVertexTangent";
    private static final String EDGE_POSITION_ATTRIBUTE_NAME = "aEdgePosition";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String NORM_UNIFORM_NAME = "uNMatrix";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String GROUND_SAMPLER_UNIFORM_NAME = "uSamplerGround";
    private static final String SLOPE_SAMPLER_UNIFORM_NAME = "uSamplerSlope";
    private static final String BOTTOM_SAMPLER_UNIFORM_NAME = "uSamplerBottom";
    private static final String SLOPE_BUMP_MAP_SAMPLER_UNIFORM_NAME = "uSamplerSlopePumpMap";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIFFUSE_WEIGHT_FACTOR = "diffuseWeightFactor";
    private static final String UNIFORM_EDGE_DISTANCE = "uEdgeDistance";
    private static final String UNIFORM_MVP_SHADOW_BIAS = "uMVPDepthBias";
    private static final String UNIFORM_SHADOW_MAP_SAMPLER = "uSamplerShadow";
    private static final String UNIFORM_SHADOW_ALPHA = "uShadowAlpha";
    private static final String UNIFORM_BUMP_MAP_DEPTH = "bumpMapDepth";
    private static final String UNIFORM_SLOPE_TOP_THRESHOLD = "slopeTopThreshold";
    private static final String UNIFORM_SLOPE_TOP_THRESHOLD_FADING = "slopeTopThresholdFading";
    private static final String UNIFORM_SLOPE_SPECULAR_HARDNESS = "uSlopeSpecularHardness";
    private static final String UNIFORM_SLOPE_SPECULAR_INTENSITY = "uSlopeSpecularIntensity";


    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer normalBuffer;
    private int normalPositionAttribute;
    private WebGLBuffer tangentBuffer;
    private int tangentPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private int edgePositionAttribute;
    private WebGLBuffer edgeBuffer;
    private WebGLTexture groundWebGLTexture;
    private WebGLTexture slopeWebGLTexture;
    private WebGLTexture bottomWebGLTexture;
    private WebGLTexture slopeBumpMapWebGLTexture;
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

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.terrainSurfaceVertexShader(), Shaders.INSTANCE.terrainSurfaceFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        normalBuffer = gameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        tangentBuffer = gameCanvas.getCtx3d().createBuffer();
        tangentPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_TANGENT);
        edgeBuffer = gameCanvas.getCtx3d().createBuffer();
        edgePositionAttribute = getAndEnableAttributeLocation(EDGE_POSITION_ATTRIBUTE_NAME);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);

        groundWebGLTexture = setupTexture(terrainSurface.getGroundImageDescriptor());
        bottomWebGLTexture = setupTexture(terrainSurface.getBottomImageDescriptor());
        slopeWebGLTexture = setupTexture(terrainSurface.getSlopeImageDescriptor());
        slopeBumpMapWebGLTexture = setupTextureForBumpMap(terrainSurface.getSlopePumpMapImageDescriptor());
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

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, tangentBuffer);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTangentPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
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
        // Norm matrix transformation uniform
        WebGLUniformLocation mNUniform = getUniformLocation(NORM_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mNUniform, false, WebGlUtil.createArrayBufferOfFloat32(camera.createNormMatrix().toWebGlArray()));

        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getUniformLocation(UNIFORM_AMBIENT_COLOR);
        gameCanvas.getCtx3d().uniform3f(pAmbientUniformColor, (float) lighting.getAmbientIntensity(), (float) lighting.getAmbientIntensity(), (float) lighting.getAmbientIntensity());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        gameCanvas.getCtx3d().uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pUnifromDiffuseWeigthFactor = getUniformLocation(UNIFORM_DIFFUSE_WEIGHT_FACTOR);
        gameCanvas.getCtx3d().uniform1f(pUnifromDiffuseWeigthFactor, (float) lighting.getDiffuseIntensity());
        // Edges
        WebGLUniformLocation edgeDistanceUniform = getUniformLocation(UNIFORM_EDGE_DISTANCE);
        gameCanvas.getCtx3d().uniform1f(edgeDistanceUniform, (float) terrainSurface.getEdgeDistance());

        // Slope top threshold
        WebGLUniformLocation slopeTopThreshold = getUniformLocation(UNIFORM_SLOPE_TOP_THRESHOLD);
        gameCanvas.getCtx3d().uniform1f(slopeTopThreshold, (float) terrainSurface.getPlateau().getSlopeTopThreshold());
        WebGLUniformLocation slopeTopThresholdFading = getUniformLocation(UNIFORM_SLOPE_TOP_THRESHOLD_FADING);
        gameCanvas.getCtx3d().uniform1f(slopeTopThresholdFading, (float) terrainSurface.getPlateau().getSlopeTopThresholdFading());

        // Bump mapping
        WebGLUniformLocation bumpMapDepthUniform = getUniformLocation(UNIFORM_BUMP_MAP_DEPTH);
        gameCanvas.getCtx3d().uniform1f(bumpMapDepthUniform, (float) terrainSurface.getPlateau().getBumpMapDepth());

        // Specular
        WebGLUniformLocation slopeSpecularHardness = getUniformLocation(UNIFORM_SLOPE_SPECULAR_HARDNESS);
        gameCanvas.getCtx3d().uniform1f(slopeSpecularHardness, (float) terrainSurface.getPlateau().getSpecularHardness());
        WebGLUniformLocation slopeSpecularIntensity = getUniformLocation(UNIFORM_SLOPE_SPECULAR_INTENSITY);
        gameCanvas.getCtx3d().uniform1f(slopeSpecularIntensity, (float) terrainSurface.getPlateau().getSpecularIntensity());

        // Shadow
        WebGLUniformLocation shadowMvpUniform = getUniformLocation(UNIFORM_MVP_SHADOW_BIAS);
        gameCanvas.getCtx3d().uniformMatrix4fv(shadowMvpUniform, false, WebGlUtil.createArrayBufferOfFloat32(lighting.createViewProjectionTransformation().toWebGlArray()));
        WebGLUniformLocation shadowMapUniform = getUniformLocation(UNIFORM_SHADOW_MAP_SAMPLER);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE4);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        gameCanvas.getCtx3d().uniform1i(shadowMapUniform, 4);
        WebGLUniformLocation uniformShadowAlpha = getUniformLocation(UNIFORM_SHADOW_ALPHA);
        gameCanvas.getCtx3d().uniform1f(uniformShadowAlpha, (float) lighting.getShadowAlpha());

        // Positions
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Set the normals
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Set the tangent
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, tangentBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(tangentPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Edges
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, edgeBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(edgePositionAttribute, 1, WebGLRenderingContext.FLOAT, false, 0, 0);
        // Textures
        WebGLUniformLocation tTopUniform = getUniformLocation(GROUND_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, groundWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tTopUniform, 0);

        WebGLUniformLocation tBlendUniform = getUniformLocation(SLOPE_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE1);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, slopeWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tBlendUniform, 1);

        WebGLUniformLocation tBottomUniform = getUniformLocation(BOTTOM_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE2);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, bottomWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tBottomUniform, 2);

        WebGLUniformLocation tBumpMapUniform = getUniformLocation(SLOPE_BUMP_MAP_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE3);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, slopeBumpMapWebGLTexture);
        gameCanvas.getCtx3d().uniform1i(tBumpMapUniform, 3);

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
