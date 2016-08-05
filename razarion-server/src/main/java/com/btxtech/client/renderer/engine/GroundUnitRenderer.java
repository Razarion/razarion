package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainUiService;
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
public class GroundUnitRenderer extends AbstractWebGlUnitRenderer {
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute splattings;
    private WebGlUniformTexture_OLD topTexture;
    private WebGlUniformTexture_OLD topBm;
    private WebGlUniformTexture_OLD splattingTexture;
    private WebGlUniformTexture_OLD bottomTexture;
    private WebGlUniformTexture_OLD bottomBm;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainUiService terrainUiService;
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
        topTexture = createWebGLTexture(terrainUiService.getTopTexture(), "uTopTexture");
        topBm = createWebGLBumpMapTexture(terrainUiService.getTopBm(), "uTopBm");
        splattingTexture = createWebGLTexture(terrainUiService.getSplatting(), "uSplatting");
        bottomTexture = createWebGLTexture(terrainUiService.getGroundTexture(), "uBottomTexture");
        bottomBm = createWebGLBumpMapTexture(terrainUiService.getGroundBm(), "uBottomBm");
        enableShadow();
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainUiService.getGroundVertexList();

        vertices.fillBuffer(vertexList.getVertices());
        normals.fillBuffer(vertexList.getNormVertices());
        tangents.fillBuffer(vertexList.getTangentVertices());
        splattings.fillDoubleBuffer(vertexList.getSplattings());

        setElementCount(vertexList.getVerticesCount());
    }

    @Override
    public void draw() {
        useProgram();
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());

        setLightUniforms(null, terrainTypeService.getGroundSkeletonConfig().getLightConfig());
        uniform1f("uTopBmDepth", terrainTypeService.getGroundSkeletonConfig().getTopBmDepth());
        uniform1f("uBottomBmDepth", terrainTypeService.getGroundSkeletonConfig().getBottomBmDepth());
        uniform1i("uTopTextureSize", terrainUiService.getTopTexture().getQuadraticEdge());
        uniform1i("uBottomTextureSize", terrainUiService.getGroundTexture().getQuadraticEdge());
        uniform1i("uTopBmSize", terrainUiService.getTopBm().getQuadraticEdge());
        uniform1i("uBottomBmSize", terrainUiService.getGroundBm().getQuadraticEdge());
        uniform1i("uSplattingSize", terrainUiService.getSplatting().getQuadraticEdge());

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
        drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
