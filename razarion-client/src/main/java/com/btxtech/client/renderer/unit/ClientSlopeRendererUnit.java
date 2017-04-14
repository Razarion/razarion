package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

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
    private WebGlFacade webGlFacade;
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
    private LightUniforms slopeLightUniforms;
    private LightUniforms groundLightUniforms;
    private WebGLUniformLocation slopeOriented;
    private WebGLUniformLocation uSlopeBmDepth;
    private WebGLUniformLocation uGroundTopBmDepth;
    private WebGLUniformLocation uGroundBottomBmDepth;
    private WebGLUniformLocation uHasWater;
    private WebGLUniformLocation uWaterLevel;
    private WebGLUniformLocation uWaterGround;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader()).enableTransformation(true).enableReceiveShadow());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloat32ArrayShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloat32ArrayShaderAttribute("aGroundSplatting");
        slopeLightUniforms = new LightUniforms("Slope", webGlFacade);
        groundLightUniforms = new LightUniforms("Ground", webGlFacade);
        slopeOriented = webGlFacade.getUniformLocation("slopeOriented");
        uSlopeBmDepth = webGlFacade.getUniformLocation("uSlopeBmDepth");
        uGroundTopBmDepth = webGlFacade.getUniformLocation("uGroundTopBmDepth");
        uGroundBottomBmDepth = webGlFacade.getUniformLocation("uGroundBottomBmDepth");
        uHasWater = webGlFacade.getUniformLocation("uHasWater");
        uWaterLevel = webGlFacade.getUniformLocation("uWaterLevel");
        uWaterGround = webGlFacade.getUniformLocation("uWaterGround");
    }

    @Override
    protected void fillBuffer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        slopeTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getTextureId(), "uSlopeTexture", "uSlopeTextureScale", uiTerrainSlopeTile.getTextureScale());
        uSlopeBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getBmId(), "uSlopeBm", "uSlopeBmScale", uiTerrainSlopeTile.getBmScale(), "uSlopeBmOnePixel");

        groundTopTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getTopTextureId(), "uGroundTopTexture", "uGroundTopTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
        groundTopBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getUiTerrainTile().getTopBmId(), "uGroundTopBm", "uGroundTopBmScale", uiTerrainSlopeTile.getUiTerrainTile().getTopBmScale(), "uGroundTopBmOnePixel");
        groundSplattingTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getSplattingId(), "uGroundSplatting", "uGroundSplattingScale", uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
        groundBottomTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureId(), "uGroundBottomTexture", "uGroundBottomTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
        groundBottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmId(), "uGroundBottomBm", "uGroundBottomBmScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale(), "uGroundBottomBmOnePixel");

        vertices.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getVertices()));
        normals.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getNorms()));
        tangents.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getTangents()));
        slopeFactors.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getSlopeFactors()));
        groundSplatting.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getGroundSplattings()));
    }


    @Override
    protected void draw(UiTerrainSlopeTile uiTerrainSlopeTile) {
        webGlFacade.useProgram();

        slopeLightUniforms.setLightUniforms(uiTerrainSlopeTile.getSlopeLightConfig(), webGlFacade);
        groundLightUniforms.setLightUniforms(uiTerrainSlopeTile.getUiTerrainTile().getGroundLightConfig(), webGlFacade);

        webGlFacade.uniform1b(slopeOriented, uiTerrainSlopeTile.isSlopeOriented());

        // Slope
        webGlFacade.uniform1f(uSlopeBmDepth, uiTerrainSlopeTile.getBmDepth());
        //Ground
        webGlFacade.uniform1f(uGroundTopBmDepth, uiTerrainSlopeTile.getUiTerrainTile().getTopBmDepth());
        webGlFacade.uniform1f(uGroundBottomBmDepth, uiTerrainSlopeTile.getUiTerrainTile().getBottomBmDepth());
        // Water
        webGlFacade.uniform1b(uHasWater, uiTerrainSlopeTile.hasWater());
        webGlFacade.uniform1f(uWaterLevel, uiTerrainSlopeTile.getWaterLevel());
        webGlFacade.uniform1f(uWaterGround, uiTerrainSlopeTile.getWaterGroundLevel());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.overrideScale(uiTerrainSlopeTile.getTextureScale());
        slopeTexture.activate();
        uSlopeBm.overrideScale(uiTerrainSlopeTile.getBmScale());
        uSlopeBm.activate();

        groundTopTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
        groundTopTexture.activate();
        groundTopBm.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getTopBmScale());
        groundTopBm.activate();
        groundSplattingTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
        groundSplattingTexture.activate();
        groundBottomTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
        groundBottomTexture.activate();
        groundBottomBm.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale());
        groundBottomBm.activate();

        webGlFacade.activateReceiveShadow();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void dispose() {
        vertices.deleteBuffer();
        normals.deleteBuffer();
        tangents.deleteBuffer();
        groundSplatting.deleteBuffer();
        slopeFactors.deleteBuffer();
    }

}
