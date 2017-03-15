package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;
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
    private TerrainUiService terrainUiService;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private Vec3Float32ArrayShaderAttribute vertices;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec3Float32ArrayShaderAttribute tangents;
    private Float32ArrayShaderAttribute slopeFactors;
    private Float32ArrayShaderAttribute groundSplatting;
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
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloat32ArrayShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloat32ArrayShaderAttribute("aGroundSplatting");
        webGlFacade.enableReceiveShadow();
    }

    @Override
    protected void fillBuffer(SlopeUi slopeUi) {
        slopeTexture = webGlFacade.createWebGLTexture(slopeUi.getTextureId(), "uSlopeTexture", "uSlopeTextureScale", slopeUi.getTextureScale());
        uSlopeBm = webGlFacade.createWebGLBumpMapTexture(slopeUi.getBmId(), "uSlopeBm", "uSlopeBmScale", slopeUi.getBmScale(), "uSlopeBmOnePixel");

        groundTopTexture = webGlFacade.createWebGLTexture(slopeUi.getTopTextureId(), "uGroundTopTexture", "uGroundTopTextureScale", slopeUi.getTopTextureScale());
        groundTopBm = webGlFacade.createWebGLBumpMapTexture(slopeUi.getTopBmId(), "uGroundTopBm", "uGroundTopBmScale", slopeUi.getTopBmScale(), "uGroundTopBmOnePixel");
        groundSplattingTexture = webGlFacade.createWebGLTexture(slopeUi.getSplattingId(), "uGroundSplatting", "uGroundSplattingScale", slopeUi.getSplattingScale());
        groundBottomTexture = webGlFacade.createWebGLTexture(slopeUi.getBottomTextureId(), "uGroundBottomTexture", "uGroundBottomTextureScale", slopeUi.getBottomTextureScale());
        groundBottomBm = webGlFacade.createWebGLBumpMapTexture(slopeUi.getBottomBmId(), "uGroundBottomBm", "uGroundBottomBmScale", slopeUi.getBottomBmScale(), "uGroundBottomBmOnePixel");

        vertices.fillFloat32ArrayEmu(slopeUi.getVertices());
        normals.fillFloat32ArrayEmu(slopeUi.getNorms());
        tangents.fillFloat32ArrayEmu(slopeUi.getTangents());
        slopeFactors.fillFloat32ArrayEmu(slopeUi.getSlopeFactors());
        groundSplatting.fillFloat32ArrayEmu(slopeUi.getSplatting());
    }


    @Override
    protected void draw(SlopeUi slopeUi) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.getNormMatrix());

        webGlFacade.setLightUniforms("Slope", slopeUi.getSlopeLightConfig());
        webGlFacade.setLightUniforms("Ground", slopeUi.getGroundLightConfig());

        webGlFacade.uniform1b("slopeOriented", slopeUi.isSlopeOriented());

        // Slope
        webGlFacade.uniform1f("uSlopeBmDepth", slopeUi.getBmDepth());
        //Ground
        webGlFacade.uniform1f("uGroundTopBmDepth", slopeUi.getTopBmDepth());
        webGlFacade.uniform1f("uGroundBottomBmDepth", slopeUi.getBottomBmDepth());
        // Water
        webGlFacade.uniform1b("uHasWater", slopeUi.hasWater());
        webGlFacade.uniform1f("uWaterLevel", slopeUi.getWaterLevel());
        webGlFacade.uniform1f("uWaterGround", terrainUiService.getWaterUi().getGroundLevel());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.overrideScale(slopeUi.getTextureScale());
        slopeTexture.activate();
        uSlopeBm.overrideScale(slopeUi.getBmScale());
        uSlopeBm.activate();

        groundTopTexture.overrideScale(slopeUi.getTopTextureScale());
        groundTopTexture.activate();
        groundTopBm.overrideScale(slopeUi.getTopBmScale());
        groundTopBm.activate();
        groundSplattingTexture.overrideScale(slopeUi.getSplattingScale());
        groundSplattingTexture.activate();
        groundBottomTexture.overrideScale(slopeUi.getBottomTextureScale());
        groundBottomTexture.activate();
        groundBottomBm.overrideScale(slopeUi.getBottomBmScale());
        groundBottomBm.activate();

        webGlFacade.activateReceiveShadow();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void setupImages() {

    }
}
