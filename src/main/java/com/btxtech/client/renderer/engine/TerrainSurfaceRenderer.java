package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;
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
    private static final String A_SLOPE_FACTOR = "aSlopeFactor";
    private static final String A_TYPE = "aType";
    private static final String EDGE_POSITION_ATTRIBUTE_NAME = "aEdgePosition";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String NORM_UNIFORM_NAME = "uNMatrix";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String GROUND_SAMPLER_UNIFORM_NAME = "uSamplerGround";
    private static final String GROUND_BM_SAMPLER_UNIFORM_NAME = "uSamplerGroundBm";
    private static final String UNIFORM_BUMP_MAP_DEPTH_GROUND = "bumpMapDepthGround";
    private static final String SLOPE_SAMPLER_UNIFORM_NAME = "uSamplerSlope";
    private static final String SLOPE_BUMP_MAP_SAMPLER_UNIFORM_NAME = "uSamplerSlopePumpMap";
    private static final String UNIFORM_BUMP_MAP_DEPTH_SLOPE = "bumpMapDepthSlope";
    private static final String BEACH_SAMPLER_UNIFORM_NAME = "uSamplerBeach";
    private static final String BEACH_BUMP_MAP_SAMPLER_UNIFORM_NAME = "uSamplerBeachPumpMap";
    private static final String UNIFORM_BUMP_MAP_DEPTH_BEACH = "bumpMapDepthBeach";
    private static final String COLVER_SAMPLER_UNIFORM_NAME = "uSamplerCover";
    private static final String BLENDER_SAMPLER_UNIFORM_NAME = "uSamplerBlender";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIFFUSE_WEIGHT_FACTOR = "diffuseWeightFactor";
    private static final String UNIFORM_EDGE_DISTANCE = "uEdgeDistance";
    private static final String UNIFORM_MVP_SHADOW_BIAS = "uMVPDepthBias";
    private static final String UNIFORM_SHADOW_MAP_SAMPLER = "uSamplerShadow";
    private static final String UNIFORM_SHADOW_ALPHA = "uShadowAlpha";
    private static final String UNIFORM_SLOPE_SPECULAR_HARDNESS = "uSlopeSpecularHardness";
    private static final String UNIFORM_SLOPE_SPECULAR_INTENSITY = "uSlopeSpecularIntensity";
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute edges;
    private FloatShaderAttribute slopes;
    private FloatShaderAttribute types;
    private WebGlUniformTexture coverWebGLTexture;
    private WebGlUniformTexture blenderWebGLTexture;
    private WebGlUniformTexture groundWebGLTexture;
    private WebGlUniformTexture groundBmWebGLTexture;
    private WebGlUniformTexture slopeWebGLTexture;
    private WebGlUniformTexture slopeBumpMapWebGLTexture;
    private WebGlUniformTexture beachWebGLTexture;
    private WebGlUniformTexture beachBumpMapWebGLTexture;
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
        createProgram(Shaders.INSTANCE.terrainSurfaceVertexShader(), Shaders.INSTANCE.terrainSurfaceFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute(A_VERTEX_NORMAL);
        tangents = createVertexShaderAttribute(A_VERTEX_TANGENT);
        edges = createFloatShaderAttribute(EDGE_POSITION_ATTRIBUTE_NAME);
        slopes = createFloatShaderAttribute(A_SLOPE_FACTOR);
        types = createFloatShaderAttribute(A_TYPE);

        coverWebGLTexture = createWebGLTexture(terrainSurface.getCoverImageDescriptor(), COLVER_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE1, 1);
        blenderWebGLTexture = createWebGLTexture(terrainSurface.getBlenderImageDescriptor(), BLENDER_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE2, 2);
        groundWebGLTexture = createWebGLTexture(terrainSurface.getGroundImageDescriptor(), GROUND_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE3, 3);
        groundBmWebGLTexture = createWebGLBumpMapTexture(terrainSurface.getGroundBmImageDescriptor(), GROUND_BM_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE4, 4);
        slopeWebGLTexture = createWebGLTexture(terrainSurface.getSlopeImageDescriptor(), SLOPE_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE5, 5);
        slopeBumpMapWebGLTexture = createWebGLBumpMapTexture(terrainSurface.getSlopePumpMapImageDescriptor(), SLOPE_BUMP_MAP_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE6, 6);
        beachWebGLTexture = createWebGLTexture(terrainSurface.getBeachImageDescriptor(), BEACH_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE7, 7);
        beachBumpMapWebGLTexture = createWebGLBumpMapTexture(terrainSurface.getBeachPumpMapImageDescriptor(), BEACH_BUMP_MAP_SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE8, 8);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();

        vertices.fillBuffer(vertexList.getVertices());
        normals.fillBuffer(vertexList.getNormVertices());
        tangents.fillBuffer(vertexList.getTangentVertices());
        edges.fillBuffer(vertexList.getEdges());
        slopes.fillBuffer(vertexList.getSlopeFactor());
        types.fillBuffer(vertexList.getTypesAsDoubles());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());
        uniformMatrix4fv(NORM_UNIFORM_NAME, camera.createNormMatrix());
        uniform3f(UNIFORM_AMBIENT_COLOR, lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        Vertex direction = lighting.getLightDirection();
        uniform3f(UNIFORM_LIGHTING_DIRECTION, direction.getX(), direction.getY(), direction.getZ());
        uniform1f(UNIFORM_DIFFUSE_WEIGHT_FACTOR, lighting.getDiffuseIntensity());
        uniform1f(UNIFORM_EDGE_DISTANCE, terrainSurface.getEdgeDistance());
        uniform1f(UNIFORM_BUMP_MAP_DEPTH_GROUND, terrainSurface.getGroundBumpMap());
        uniform1f(UNIFORM_BUMP_MAP_DEPTH_SLOPE, terrainSurface.getPlateau().getPlateauConfigEntity().getBumpMapDepth());
        uniform1f(UNIFORM_BUMP_MAP_DEPTH_BEACH, terrainSurface.getBeachBumpMap());
        uniform1f(UNIFORM_SLOPE_SPECULAR_HARDNESS, terrainSurface.getPlateau().getPlateauConfigEntity().getSpecularHardness());
        uniform1f(UNIFORM_SLOPE_SPECULAR_INTENSITY, terrainSurface.getPlateau().getPlateauConfigEntity().getSpecularIntensity());

        // Shadow
        // TODO make simpler
        uniformMatrix4fv(UNIFORM_MVP_SHADOW_BIAS, lighting.createViewProjectionTransformation());
        WebGLUniformLocation shadowMapUniform = getUniformLocation(UNIFORM_SHADOW_MAP_SAMPLER);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        gameCanvas.getCtx3d().uniform1i(shadowMapUniform, 0);
        WebGLUniformLocation uniformShadowAlpha = getUniformLocation(UNIFORM_SHADOW_ALPHA);
        gameCanvas.getCtx3d().uniform1f(uniformShadowAlpha, (float) lighting.getShadowAlpha());

        vertices.activate();
        normals.activate();
        tangents.activate();
        edges.activate();
        slopes.activate();
        types.activate();

        coverWebGLTexture.activate();
        blenderWebGLTexture.activate();
        groundWebGLTexture.activate();
        groundBmWebGLTexture.activate();
        slopeWebGLTexture.activate();
        slopeBumpMapWebGLTexture.activate();
        beachWebGLTexture.activate();
        beachBumpMapWebGLTexture.activate();

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
