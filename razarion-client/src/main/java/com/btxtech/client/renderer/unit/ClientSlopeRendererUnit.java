package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
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
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Vec3Float32ArrayShaderAttribute vertices;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec3Float32ArrayShaderAttribute tangents;
    private Float32ArrayShaderAttribute slopeFactors;
    private Float32ArrayShaderAttribute groundSplatting;
    private WebGlUniformTexture slopeTexture;
    private WebGlUniformTexture uSlopeBm;
    private SpecularUniforms slopeSpecularUniforms;
    private WebGLUniformLocation uSlopeBmDepth;
    private WebGlUniformTexture slopeWaterSplatting;
    private WebGLUniformLocation slopeWaterSplattingFactor;
    private WebGLUniformLocation slopeWaterSplattingFadeThreshold;
    private WebGLUniformLocation slopeWaterSplattingHeight;
    private LightUniforms lightUniforms;
    private SpecularUniforms groundSpecularUniforms;
    private WebGlUniformTexture groundSplattingTexture;
    private WebGlUniformTexture groundTopTexture;
    private WebGlUniformTexture groundBottomTexture;
    private WebGlUniformTexture groundBottomBm;
    private WebGLUniformLocation uGroundSplattingFadeThreshold;
    private WebGLUniformLocation uGroundSplattingOffset;
    private WebGLUniformLocation uGroundSplattingGroundBmMultiplicator;
    private WebGLUniformLocation uGroundBottomBmDepth;
    private WebGLUniformLocation uHasWater;
    private WebGLUniformLocation uWaterLevel;
    private WebGLUniformLocation uWaterGround;
    private WebGLUniformLocation uWaterLightSpecularIntensity;
    private WebGLUniformLocation uWaterLightSpecularHardness;
    private WebGlUniformTexture waterReflection;
    private WebGlUniformTexture waterBumpMap;
    private WebGlUniformTexture waterDistortionMap;
    private WebGLUniformLocation uWaterTransparency;
    private WebGLUniformLocation waterDistortionStrength;
    private WebGLUniformLocation waterNormMapDepth;
    private WebGLUniformLocation waterAnimation;
    private WebGlUniformTexture terrainMarkerTexture;
    private WebGLUniformLocation terrainMarker2DPoints;
    private WebGLUniformLocation terrainMarkerAnimation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader()).enableTransformation(true).enableReceiveShadow());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloat32ArrayShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloat32ArrayShaderAttribute("aGroundSplatting");
        lightUniforms = new LightUniforms(null, webGlFacade);
        uGroundSplattingFadeThreshold = webGlFacade.getUniformLocation("uGroundSplattingFadeThreshold");
        uGroundSplattingOffset = webGlFacade.getUniformLocation("uGroundSplattingOffset");
        uGroundSplattingGroundBmMultiplicator = webGlFacade.getUniformLocation("uGroundSplattingGroundBmMultiplicator");
        slopeSpecularUniforms = new SpecularUniforms("Slope", webGlFacade);
        groundSpecularUniforms = new SpecularUniforms("Ground", webGlFacade);
        uSlopeBmDepth = webGlFacade.getUniformLocation("uSlopeBmDepth");
        slopeWaterSplattingFactor = webGlFacade.getUniformLocation("uSlopeWaterSplattingFactor");
        slopeWaterSplattingFadeThreshold = webGlFacade.getUniformLocation("uSlopeWaterSplattingFadeThreshold");
        slopeWaterSplattingHeight = webGlFacade.getUniformLocation("uSlopeWaterSplattingHeight");
        uGroundBottomBmDepth = webGlFacade.getUniformLocation("uGroundBottomBmDepth");
        uHasWater = webGlFacade.getUniformLocation("uHasWater");
        uWaterLevel = webGlFacade.getUniformLocation("uWaterLevel");
        uWaterGround = webGlFacade.getUniformLocation("uWaterGround");
        uWaterLightSpecularIntensity = webGlFacade.getUniformLocation("uWaterLightSpecularIntensity");
        uWaterLightSpecularHardness = webGlFacade.getUniformLocation("uWaterLightSpecularHardness");
        uWaterTransparency = webGlFacade.getUniformLocation("uWaterTransparency");
        waterDistortionStrength = webGlFacade.getUniformLocation("uWaterDistortionStrength");
        waterNormMapDepth = webGlFacade.getUniformLocation("uWaterNormMapDepth");
        waterAnimation = webGlFacade.getUniformLocation("uWaterAnimation");
        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    protected void fillBuffer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        slopeTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getTextureId(), "uSlopeTexture", "uSlopeTextureScale", uiTerrainSlopeTile.getTextureScale());
        // TODO uSlopeBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getBmId(), "uSlopeBm", "uSlopeBmScale", uiTerrainSlopeTile.getBmScale(), "uSlopeBmOnePixel");
        slopeWaterSplatting= webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingId(), "uSlopeWaterSplatting", "uSlopeWaterSplattingScale", uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingScale());

        groundTopTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getTopTextureId(), "uGroundTopTexture", "uGroundTopTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
        groundSplattingTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getSplattingId(), "uGroundSplatting", "uGroundSplattingScale", uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
        groundBottomTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureId(), "uGroundBottomTexture", "uGroundBottomTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
        groundBottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmId(), "uGroundBottomBm", "uGroundBottomBmScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale(), "uGroundBottomBmOnePixel");

        // TODO vertices.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getVertices()));
        // TODO normals.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getNorms()));
        // TODO tangents.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getTangents()));
        // TODO slopeFactors.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getSlopeFactors()));
        // TODO groundSplatting.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getGroundSplattings()));

        waterReflection = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getReflectionId(), "uWaterReflection", "uWaterReflectionScale", uiTerrainSlopeTile.getWaterConfig().getReflectionScale());
        waterBumpMap = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getNormMapId(), "uWaterNormMap");
        waterDistortionMap = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getDistortionId(), "uWaterDistortionMap", "uWaterDistortionScale", uiTerrainSlopeTile.getWaterConfig().getDistortionScale());
    }


    @Override
    protected void draw(UiTerrainSlopeTile uiTerrainSlopeTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(webGlFacade);

        // Slope
        slopeSpecularUniforms.setUniforms(uiTerrainSlopeTile.getSlopeLightConfig(), webGlFacade);
        webGlFacade.uniform1f(uSlopeBmDepth, uiTerrainSlopeTile.getBmDepth());
        webGlFacade.uniform1f(slopeWaterSplattingFactor, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingFactor());
        webGlFacade.uniform1f(slopeWaterSplattingFadeThreshold, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingFadeThreshold());
        webGlFacade.uniform1f(slopeWaterSplattingHeight, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingHeight());
        //Ground
        groundSpecularUniforms.setUniforms(uiTerrainSlopeTile.getUiTerrainTile().getSpecularLightConfig(), webGlFacade);
        webGlFacade.uniform1f(uGroundBottomBmDepth, uiTerrainSlopeTile.getUiTerrainTile().getBottomBmDepth());
        // TODO webGlFacade.uniform1f(uGroundSplattingFadeThreshold, uiTerrainSlopeTile.getUiTerrainTile().getSplattingFadeThreshold());
        // TODO webGlFacade.uniform1f(uGroundSplattingOffset, uiTerrainSlopeTile.getUiTerrainTile().getSplattingOffset());
        webGlFacade.uniform1f(uGroundSplattingGroundBmMultiplicator, uiTerrainSlopeTile.getUiTerrainTile().getSplattingGroundBmMultiplicator());

        // Water
        webGlFacade.uniform1b(uHasWater, uiTerrainSlopeTile.hasWater());
        webGlFacade.uniform1f(uWaterLevel, uiTerrainSlopeTile.getWaterConfig().getWaterLevel());
        webGlFacade.uniform1f(uWaterGround, uiTerrainSlopeTile.getWaterConfig().getGroundLevel());
        webGlFacade.uniform1f(uWaterLightSpecularIntensity, uiTerrainSlopeTile.getWaterConfig().getSpecularLightConfig().getSpecularIntensity());
        webGlFacade.uniform1f(uWaterLightSpecularHardness, uiTerrainSlopeTile.getWaterConfig().getSpecularLightConfig().getSpecularHardness());
        webGlFacade.uniform1f(uWaterTransparency, uiTerrainSlopeTile.getWaterConfig().getTransparency());
        webGlFacade.uniform1f(waterDistortionStrength, uiTerrainSlopeTile.getWaterConfig().getDistortionStrength());
        webGlFacade.uniform1f(waterNormMapDepth, uiTerrainSlopeTile.getWaterConfig().getNormMapDepth());

        webGlFacade.uniform1f(waterAnimation, uiTerrainSlopeTile.getWaterAnimation());
        waterReflection.overrideScale(uiTerrainSlopeTile.getWaterConfig().getReflectionScale());
        waterReflection.activate();
        waterBumpMap.activate();
        waterDistortionMap.overrideScale(uiTerrainSlopeTile.getWaterConfig().getDistortionScale());
        waterDistortionMap.activate();

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.overrideScale(uiTerrainSlopeTile.getTextureScale());
        slopeTexture.activate();
        // TODO uSlopeBm.overrideScale(uiTerrainSlopeTile.getBmScale());
        uSlopeBm.activate();

        slopeWaterSplatting.activate();
        slopeWaterSplatting.overrideScale(uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingScale());

        groundTopTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
        groundTopTexture.activate();
        groundSplattingTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
        groundSplattingTexture.activate();
        groundBottomTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
        groundBottomTexture.activate();
        groundBottomBm.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale());
        groundBottomBm.activate();
        webGlFacade.activateReceiveShadow();

        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
            terrainMarkerTexture.activate();
            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
        } else {
            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
        }

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
