package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@Dependent
public class GroundRenderer extends AbstractRenderer {
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute splattings;
    private WebGlUniformTexture topTexture;
    private WebGlUniformTexture topBm;
    private WebGlUniformTexture splattingTexture;
    private WebGlUniformTexture bottomTexture;
    private WebGlUniformTexture bottomBm;
    private int elementCount;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
        splattings = createFloatShaderAttribute("aGroundSplatting");
    }

    @Override
    public void setupImages() {
        topTexture = createWebGLTexture(terrainSurface.getTopTexture(), "uTopTexture");
        topBm = createWebGLBumpMapTexture(terrainSurface.getTopBm(), "uTopBm");
        splattingTexture = createWebGLTexture(terrainSurface.getSplatting(), "uSplatting");
        bottomTexture = createWebGLTexture(terrainSurface.getGroundTexture(), "uBottomTexture");
        bottomBm = createWebGLBumpMapTexture(terrainSurface.getGroundBm(), "uBottomBm");
        enableShadow();
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getGroundVertexList();

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

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());

        setLightUniforms(null, terrainSurface.getGroundSkeleton().getLightConfig());
        uniform1f("uTopBmDepth", terrainSurface.getGroundSkeleton().getTopBmDepth());
        uniform1f("uBottomBmDepth", terrainSurface.getGroundSkeleton().getBottomBmDepth());
        uniform1i("uTopTextureSize", terrainSurface.getTopTexture().getQuadraticEdge());
        uniform1i("uBottomTextureSize", terrainSurface.getGroundTexture().getQuadraticEdge());
        uniform1i("uTopBmSize", terrainSurface.getTopBm().getQuadraticEdge());
        uniform1i("uBottomBmSize", terrainSurface.getGroundBm().getQuadraticEdge());
        uniform1i("uSplattingSize", terrainSurface.getSplatting().getQuadraticEdge());

        activateShadow();

        vertices.activate();
        normals.activate();
        tangents.activate();
        splattings.activate();

        topTexture.activate();
        topBm.activate();
        splattingTexture.activate();
        bottomTexture.activate();
        bottomBm.activate();

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
