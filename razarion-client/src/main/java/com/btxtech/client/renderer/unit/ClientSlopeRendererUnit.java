package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

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
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec2Float32ArrayShaderAttribute uvs;
    private WebGlPhongMaterial material;
    //    private Float32ArrayShaderAttribute slopeFactors;
//    private Float32ArrayShaderAttribute groundSplatting;
//    private WebGlUniformTexture slopeTexture;
    //    private WebGlUniformTexture uSlopeBm;
//    private WebGLUniformLocation uSlopeBmDepth;
//    private WebGlUniformTexture slopeWaterSplatting;
//    private WebGLUniformLocation slopeWaterSplattingFactor;
//    private WebGLUniformLocation slopeWaterSplattingFadeThreshold;
//    private WebGLUniformLocation slopeWaterSplattingHeight;
    private LightUniforms lightUniforms;
//    private WebGlUniformTexture groundSplattingTexture;
//    private WebGlUniformTexture groundTopTexture;
//    private WebGlUniformTexture groundBottomTexture;
//    private WebGlUniformTexture groundBottomBm;
//    private WebGLUniformLocation uGroundSplattingFadeThreshold;
//    private WebGLUniformLocation uGroundSplattingOffset;
//    private WebGLUniformLocation uGroundSplattingGroundBmMultiplicator;
//    private WebGLUniformLocation uGroundBottomBmDepth;
//    private WebGLUniformLocation uHasWater;
//    private WebGLUniformLocation uWaterLevel;
//    private WebGLUniformLocation uWaterGround;
//    private WebGLUniformLocation uWaterLightSpecularIntensity;
//    private WebGLUniformLocation uWaterLightSpecularHardness;
//    private WebGlUniformTexture waterReflection;
//    private WebGlUniformTexture waterBumpMap;
//    private WebGlUniformTexture waterDistortionMap;
//    private WebGLUniformLocation uWaterTransparency;
//    private WebGLUniformLocation waterDistortionStrength;
//    private WebGLUniformLocation waterNormMapDepth;
//    private WebGLUniformLocation waterAnimation;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    public void init() {
        webGlFacade.enableOESStandartDerivatives();
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader()).enableTransformation(true).enableReceiveShadow());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        uvs = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_UV);

        // TODO tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        // TODO slopeFactors = webGlFacade.createFloat32ArrayShaderAttribute("aSlopeFactor");
        // TODO groundSplatting = webGlFacade.createFloat32ArrayShaderAttribute("aGroundSplatting");
        lightUniforms = new LightUniforms(webGlFacade);
        // TODO uGroundSplattingFadeThreshold = webGlFacade.getUniformLocation("uGroundSplattingFadeThreshold");
        // TODO uGroundSplattingOffset = webGlFacade.getUniformLocation("uGroundSplattingOffset");
        // TODO uGroundSplattingGroundBmMultiplicator = webGlFacade.getUniformLocation("uGroundSplattingGroundBmMultiplicator");
        // TODO slopeSpecularUniforms = new SpecularUniforms("Slope", webGlFacade);
        // TODO groundSpecularUniforms = new SpecularUniforms("Ground", webGlFacade);
        // TODO uSlopeBmDepth = webGlFacade.getUniformLocation("uSlopeBmDepth");
        // TODO slopeWaterSplattingFactor = webGlFacade.getUniformLocation("uSlopeWaterSplattingFactor");
        // TODO slopeWaterSplattingFadeThreshold = webGlFacade.getUniformLocation("uSlopeWaterSplattingFadeThreshold");
        // TODO slopeWaterSplattingHeight = webGlFacade.getUniformLocation("uSlopeWaterSplattingHeight");
        // TODO uGroundBottomBmDepth = webGlFacade.getUniformLocation("uGroundBottomBmDepth");
        // TODO uHasWater = webGlFacade.getUniformLocation("uHasWater");
        // TODO uWaterLevel = webGlFacade.getUniformLocation("uWaterLevel");
        // TODO uWaterGround = webGlFacade.getUniformLocation("uWaterGround");
        // TODO uWaterLightSpecularIntensity = webGlFacade.getUniformLocation("uWaterLightSpecularIntensity");
        // TODO uWaterLightSpecularHardness = webGlFacade.getUniformLocation("uWaterLightSpecularHardness");
        // TODO uWaterTransparency = webGlFacade.getUniformLocation("uWaterTransparency");
        // TODO waterDistortionStrength = webGlFacade.getUniformLocation("uWaterDistortionStrength");
        // TODO waterNormMapDepth = webGlFacade.getUniformLocation("uWaterNormMapDepth");
        // TODO waterAnimation = webGlFacade.getUniformLocation("uWaterAnimation");
        // TODO terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
        // TODO terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
        // TODO terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    protected void fillBufferInternal(UiTerrainSlopeTile uiTerrainSlopeTile) {
        AlarmRaiser.onNull(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), Alarm.Type.INVALID_SLOPE_CONFIG, "No Material in SlopeConfig: ", uiTerrainSlopeTile.getSlopeConfig().getId());
        material = webGlFacade.createPhongMaterial(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), "material");

        // slopeTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getTextureId(), "uSlopeTexture", "uSlopeTextureScale", uiTerrainSlopeTile.getTextureScale());
        // TODO uSlopeBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getBmId(), "uSlopeBm", "uSlopeBmScale", uiTerrainSlopeTile.getBmScale(), "uSlopeBmOnePixel");
        //  TODO  slopeWaterSplatting= webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingId(), "uSlopeWaterSplatting", "uSlopeWaterSplattingScale", uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingScale());

        // TODO groundTopTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getTopTexture(), "uGroundTopTexture", "uGroundTopTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
        // TODO groundSplattingTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getSplattingId(), "uGroundSplatting", "uGroundSplattingScale", uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
        // TODO groundBottomTexture = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureId(), "uGroundBottomTexture", "uGroundBottomTextureScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
        // TODO groundBottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmId(), "uGroundBottomBm", "uGroundBottomBmScale", uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale(), "uGroundBottomBmOnePixel");

        Float32Array groundPositions = Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getPositions());
        positions.fillFloat32Array(groundPositions);
        normals.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getNorms()));
        uvs.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getUvs()));
        setElementCount((int) (groundPositions.length / Vertex.getComponentsPerVertex()));
        // TODO slopeFactors.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getSlopeFactors()));
        // TODO groundSplatting.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getGroundSplattings()));

//        waterReflection = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getReflectionId(), "uWaterReflection", "uWaterReflectionScale", uiTerrainSlopeTile.getWaterConfig().getReflectionScale());
//        waterBumpMap = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getNormMapId(), "uWaterNormMap");
//        waterDistortionMap = webGlFacade.createWebGLTexture(uiTerrainSlopeTile.getWaterConfig().getDistortionId(), "uWaterDistortionMap", "uWaterDistortionScale", uiTerrainSlopeTile.getWaterConfig().getDistortionScale());
    }


    @Override
    protected void draw(UiTerrainSlopeTile uiTerrainSlopeTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(webGlFacade);

        // Slope
        // TODO slopeSpecularUniforms.setUniforms(uiTerrainSlopeTile.getSlopeLightConfig(), webGlFacade);
        //  webGlFacade.uniform1f(uSlopeBmDepth, uiTerrainSlopeTile.getBmDepth());
//  TODO      webGlFacade.uniform1f(slopeWaterSplattingFactor, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingFactor());
// TODO       webGlFacade.uniform1f(slopeWaterSplattingFadeThreshold, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingFadeThreshold());
// TODO       webGlFacade.uniform1f(slopeWaterSplattingHeight, uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingHeight());
        //Ground
        // TODO groundSpecularUniforms.setUniforms(uiTerrainSlopeTile.getUiTerrainTile().getSpecularLightConfig(), webGlFacade);
        // TODO webGlFacade.uniform1f(uGroundBottomBmDepth, uiTerrainSlopeTile.getUiTerrainTile().getBottomBmDepth());
        // TODO webGlFacade.uniform1f(uGroundSplattingFadeThreshold, uiTerrainSlopeTile.getUiTerrainTile().getSplattingFadeThreshold());
        // TODO webGlFacade.uniform1f(uGroundSplattingOffset, uiTerrainSlopeTile.getUiTerrainTile().getSplattingOffset());
        // TODO webGlFacade.uniform1f(uGroundSplattingGroundBmMultiplicator, uiTerrainSlopeTile.getUiTerrainTile().getSplattingGroundBmMultiplicator());

        // Water
//        webGlFacade.uniform1b(uHasWater, uiTerrainSlopeTile.hasWater());
//        webGlFacade.uniform1f(uWaterLevel, uiTerrainSlopeTile.getWaterConfig().getWaterLevel());
//        webGlFacade.uniform1f(uWaterGround, uiTerrainSlopeTile.getWaterConfig().getGroundLevel());
//        // TODO webGlFacade.uniform1f(uWaterLightSpecularIntensity, uiTerrainSlopeTile.getWaterConfig().getSpecularLightConfig().getSpecularIntensity());
//        // TODO webGlFacade.uniform1f(uWaterLightSpecularHardness, uiTerrainSlopeTile.getWaterConfig().getSpecularLightConfig().getSpecularHardness());
//        webGlFacade.uniform1f(uWaterTransparency, uiTerrainSlopeTile.getWaterConfig().getTransparency());
//        webGlFacade.uniform1f(waterDistortionStrength, uiTerrainSlopeTile.getWaterConfig().getDistortionStrength());
//        webGlFacade.uniform1f(waterNormMapDepth, uiTerrainSlopeTile.getWaterConfig().getNormMapDepth());

//        webGlFacade.uniform1f(waterAnimation, uiTerrainSlopeTile.getWaterAnimation());
//        waterReflection.overrideScale(uiTerrainSlopeTile.getWaterConfig().getReflectionScale());
//        waterReflection.activate();
//        waterBumpMap.activate();
//        waterDistortionMap.overrideScale(uiTerrainSlopeTile.getWaterConfig().getDistortionScale());
//        waterDistortionMap.activate();

        positions.activate();
        normals.activate();
        uvs.activate();
        material.activate();
//        tangents.activate();
//        slopeFactors.activate();
//        groundSplatting.activate();

//        slopeTexture.overrideScale(uiTerrainSlopeTile.getTextureScale());
//        slopeTexture.activate();
        // TODO uSlopeBm.overrideScale(uiTerrainSlopeTile.getBmScale());
//        uSlopeBm.activate();
//
//        slopeWaterSplatting.activate();
//        //  TODO   slopeWaterSplatting.overrideScale(uiTerrainSlopeTile.getSlopeConfig().getSlopeWaterSplattingScale());
//
//        // TODO groundTopTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getTopTextureScale());
//        groundTopTexture.activate();
//        // TODO groundSplattingTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getSplattingScale());
//        groundSplattingTexture.activate();
//        // TODO groundBottomTexture.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomTextureScale());
//        groundBottomTexture.activate();
//        // TODO groundBottomBm.overrideScale(uiTerrainSlopeTile.getUiTerrainTile().getBottomBmScale());
//        groundBottomBm.activate();
//        webGlFacade.activateReceiveShadow();

//        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
//            terrainMarkerTexture.activate();
//            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
//            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
//        } else {
//            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
//        }

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        positions.deleteBuffer();
        normals.deleteBuffer();
//        tangents.deleteBuffer();
//        groundSplatting.deleteBuffer();
//        slopeFactors.deleteBuffer();
    }

}
