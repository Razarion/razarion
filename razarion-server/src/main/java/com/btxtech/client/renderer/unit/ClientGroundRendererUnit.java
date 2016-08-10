package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.AbstractGroundRendererUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientGroundRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundRendererUnit.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute splattings;
    private WebGlUniformTexture topTexture;
    private WebGlUniformTexture topBm;
    private WebGlUniformTexture splattingTexture;
    private WebGlUniformTexture bottomTexture;
    private WebGlUniformTexture bottomBm;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        splattings = webGlFacade.createFloatShaderAttribute(WebGlFacade.A_GROUND_SPLATTING);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffers(VertexList vertexList) {
        vertexList.verify();

        topTexture = webGlFacade.createWebGLTexture(terrainUiService.getTopTexture(), "uTopTexture");
        topBm = webGlFacade.createWebGLBumpMapTexture(terrainUiService.getTopBm(), "uTopBm");
        splattingTexture = webGlFacade.createWebGLTexture(terrainUiService.getSplatting(), "uSplatting");
        bottomTexture = webGlFacade.createWebGLTexture(terrainUiService.getGroundTexture(), "uBottomTexture");
        bottomBm = webGlFacade.createWebGLBumpMapTexture(terrainUiService.getGroundBm(), "uBottomBm");
        webGlFacade.enableReceiveShadow();

        vertices.fillBuffer(vertexList.getVertices());
        normals.fillBuffer(vertexList.getNormVertices());
        tangents.fillBuffer(vertexList.getTangentVertices());
        splattings.fillDoubleBuffer(vertexList.getSplattings());
    }

    @Override
    public void draw() {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.createNormMatrix());

        webGlFacade.setLightUniforms(null, terrainTypeService.getGroundSkeletonConfig().getLightConfig());
        webGlFacade.uniform1f("uTopBmDepth", terrainTypeService.getGroundSkeletonConfig().getTopBmDepth());
        webGlFacade.uniform1f("uBottomBmDepth", terrainTypeService.getGroundSkeletonConfig().getBottomBmDepth());
        webGlFacade.uniform1i("uTopTextureSize", terrainUiService.getTopTexture().getQuadraticEdge());
        webGlFacade.uniform1i("uBottomTextureSize", terrainUiService.getGroundTexture().getQuadraticEdge());
        webGlFacade.uniform1i("uTopBmSize", terrainUiService.getTopBm().getQuadraticEdge());
        webGlFacade.uniform1i("uBottomBmSize", terrainUiService.getGroundBm().getQuadraticEdge());
        webGlFacade.uniform1i("uSplattingSize", terrainUiService.getSplatting().getQuadraticEdge());

        webGlFacade.activateReceiveShadow();

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
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
