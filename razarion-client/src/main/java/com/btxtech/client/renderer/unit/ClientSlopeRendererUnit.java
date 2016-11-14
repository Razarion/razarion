package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.01.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientSlopeRendererUnit extends AbstractSlopeRendererUnit {
    // private static Logger logger = Logger.getLogger(ClientSlopeRendererUnit.class.getName());
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute slopeFactors;
    private FloatShaderAttribute groundSplatting;
    private WebGlUniformTexture slopeTexture;
    private WebGlUniformTexture uSlopeBm;
    private WebGlUniformTexture groundSplattingTexture;
    private WebGlUniformTexture groundTopTexture;
    private WebGlUniformTexture groundTopBm;
    private WebGlUniformTexture groundBottomTexture;
    private WebGlUniformTexture groundBottomBm;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloatShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloatShaderAttribute("aGroundSplatting");
        webGlFacade.enableReceiveShadow();
    }

    @Override
    protected void fillBuffer(Slope slope, Mesh mesh, GroundSkeletonConfig groundSkeletonConfig) {
        slopeTexture = webGlFacade.createWebGLTexture(slope.getSlopeSkeletonConfig().getTextureId(), "uSlopeTexture", "uSlopeTextureScale", slope.getSlopeSkeletonConfig().getTextureScale());
        uSlopeBm = webGlFacade.createWebGLBumpMapTexture(slope.getSlopeSkeletonConfig().getBmId(), "uSlopeBm", "uSlopeBmScale", slope.getSlopeSkeletonConfig().getBmScale(), "uSlopeBmOnePixel");

        groundTopTexture = webGlFacade.createWebGLTexture(groundSkeletonConfig.getTopTextureId(), "uGroundTopTexture", "uGroundTopTextureScale", groundSkeletonConfig.getTopTextureScale());
        groundTopBm = webGlFacade.createWebGLBumpMapTexture(groundSkeletonConfig.getTopBmId(), "uGroundTopBm", "uGroundTopBmScale", groundSkeletonConfig.getTopBmScale(), "uGroundTopBmOnePixel");
        groundSplattingTexture = webGlFacade.createWebGLTexture(groundSkeletonConfig.getSplattingId(), "uGroundSplatting", "uGroundSplattingScale", groundSkeletonConfig.getSplattingScale());
        groundBottomTexture = webGlFacade.createWebGLTexture(groundSkeletonConfig.getBottomTextureId(), "uGroundBottomTexture", "uGroundBottomTextureScale", groundSkeletonConfig.getBottomTextureScale());
        groundBottomBm = webGlFacade.createWebGLBumpMapTexture(groundSkeletonConfig.getBottomBmId(), "uGroundBottomBm", "uGroundBottomBmScale", groundSkeletonConfig.getBottomBmScale(), "uGroundBottomBmOnePixel");

        vertices.fillBuffer(mesh.getVertices());
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());
        groundSplatting.fillFloatBuffer(mesh.getSplatting());
    }


    @Override
    protected void draw(Slope slope, GroundSkeletonConfig groundSkeletonConfig) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.getNormMatrix());

        webGlFacade.setLightUniforms("Slope", slope.getSlopeSkeletonConfig().getLightConfig());
        webGlFacade.setLightUniforms("Ground", groundSkeletonConfig.getLightConfig());

        webGlFacade.uniform1b("slopeOriented", slope.getSlopeSkeletonConfig().getSlopeOriented());

        // Slope
        webGlFacade.uniform1f("uSlopeBmDepth", slope.getSlopeSkeletonConfig().getBmDepth());
        //Ground
        webGlFacade.uniform1f("uGroundTopBmDepth", groundSkeletonConfig.getTopBmDepth());
        webGlFacade.uniform1f("uGroundBottomBmDepth", groundSkeletonConfig.getBottomBmDepth());
        // Water
        webGlFacade.uniform1b("uHasWater", slope.hasWater());
        webGlFacade.uniform1f("uWaterLevel", slope.getWaterLevel());
        webGlFacade.uniform1f("uWaterGround", visualUiService.getVisualConfig().getWaterGroundLevel());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.activate();
        uSlopeBm.activate();
        groundSplattingTexture.activate();
        groundTopTexture.activate();
        groundBottomTexture.activate();
        groundBottomBm.activate();
        groundTopBm.activate();

        webGlFacade.activateReceiveShadow();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void setupImages() {

    }
}
