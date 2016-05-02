package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
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
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute splattings;
    private WebGlUniformTexture coverWebGLTexture;
    private WebGlUniformTexture blenderWebGLTexture;
    private WebGlUniformTexture groundWebGLTexture;
    private WebGlUniformTexture groundBmWebGLTexture;
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
    private Camera camera;
    @Inject
    private RenderService renderService;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainSurfaceVertexShader(), Shaders.INSTANCE.terrainSurfaceFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
        normals = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
        splattings = createFloatShaderAttribute("aGroundSplatting");
    }

    @Override
    public void setupImages() {
        coverWebGLTexture = createWebGLTexture(terrainSurface.getCoverImageDescriptor(), "uGroundTopTexture", WebGLRenderingContext.TEXTURE1, 1);
        blenderWebGLTexture = createWebGLTexture(terrainSurface.getBlenderImageDescriptor(), "uGroundSplatting", WebGLRenderingContext.TEXTURE2, 2);
        groundWebGLTexture = createWebGLTexture(terrainSurface.getGroundImageDescriptor(), "uGroundBottomTexture", WebGLRenderingContext.TEXTURE3, 3);
        groundBmWebGLTexture = createWebGLBumpMapTexture(terrainSurface.getGroundBmImageDescriptor(), "uGroundBottomMap", WebGLRenderingContext.TEXTURE4, 4);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();

        vertices.fillBuffer(vertexList.getVertices());
        normals.fillBuffer(vertexList.getNormVertices());
        tangents.fillBuffer(vertexList.getTangentVertices());
        splattings.fillDoubleBuffer(vertexList.getEdges());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());
        uniform3f("uAmbientColor", lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        Vertex direction = lighting.getLightDirection();
        uniform3f("uLightingDirection", direction.getX(), direction.getY(), direction.getZ());
        uniform1f("diffuseWeightFactor", lighting.getDiffuseIntensity());
        uniform1f("uGroundSplattingDistance", terrainSurface.getGroundSkeletonEntity().getSplattingDistance());
        uniform1f("uGroundBottomMapDepth", terrainSurface.getGroundSkeletonEntity().getBumpMapDepth());
        uniform1f("uGroundSpecularIntensity", terrainSurface.getGroundSkeletonEntity().getSpecularIntensity());
        uniform1f("uGroundSpecularHardness", terrainSurface.getGroundSkeletonEntity().getSpecularHardness());
        uniform1i("uGroundTopTextureSize", terrainSurface.getCoverImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundBottomTextureSize", terrainSurface.getGroundImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundBottomMapSize", terrainSurface.getGroundBmImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundSplattingSize", terrainSurface.getBlenderImageDescriptor().getQuadraticEdge());

        // Shadow
        // TODO make simpler
        uniformMatrix4fv("uMVPDepthBias", lighting.createViewProjectionTransformation());
        WebGLUniformLocation shadowMapUniform = getUniformLocation("uSamplerShadow");
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        gameCanvas.getCtx3d().uniform1i(shadowMapUniform, 0);
        WebGLUniformLocation uniformShadowAlpha = getUniformLocation("uShadowAlpha");
        gameCanvas.getCtx3d().uniform1f(uniformShadowAlpha, (float) lighting.getShadowAlpha());

        vertices.activate();
        normals.activate();
        tangents.activate();
        splattings.activate();

        coverWebGLTexture.activate();
        blenderWebGLTexture.activate();
        groundWebGLTexture.activate();
        groundBmWebGLTexture.activate();

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
