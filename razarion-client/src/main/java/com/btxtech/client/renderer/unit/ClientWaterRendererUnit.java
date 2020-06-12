package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientWaterRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(ClientWaterRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Vec3Float32ArrayShaderAttribute positions;
//    private WebGLUniformLocation uLightDirection;
//    private WebGlUniformTexture reflection;
//    private WebGlUniformTexture bumpMap;
//    private WebGlUniformTexture distortionMap;
//    private WebGLUniformLocation uTransparency;
//    private WebGLUniformLocation distortionStrength;
//    private WebGLUniformLocation normMapDepth;
//    private WebGLUniformLocation animation;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
//        uLightDirection = webGlFacade.getUniformLocation("uLightDirection");
//        uTransparency = webGlFacade.getUniformLocation("uTransparency");
//        distortionStrength = webGlFacade.getUniformLocation("uDistortionStrength");
//        normMapDepth = webGlFacade.getUniformLocation("uNormMapDepth");
//        animation = webGlFacade.getUniformLocation("animation");
//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillInternalBuffers(UiTerrainWaterTile uiTerrainWaterTile) {
        Float32Array waterPositions = Js.uncheckedCast(uiTerrainWaterTile.getPositions());
        positions.fillFloat32Array(waterPositions);
        // uvs.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getUvs()));
        setElementCount((int) (waterPositions.length / Vertex.getComponentsPerVertex()));



//        reflection = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getReflectionId(), "uReflection", "uReflectionScale", uiTerrainWaterTile.getWaterConfig().getReflectionScale());
//        bumpMap = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getNormMapId(), "uNormMap");
//        distortionMap = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getDistortionId(), "uDistortionMap", "uDistortionScale", uiTerrainWaterTile.getWaterConfig().getDistortionScale());
    }

    @Override
    public void draw(UiTerrainWaterTile uiTerrainWaterTile) {
        webGlFacade.useProgram();

//        webGlFacade.uniform3f(uLightDirection, webGlFacade.getVisualUiService().getLightDirection());
        // TODO webGlFacade.uniform1f(uLightSpecularIntensity, uiTerrainWaterTile.getWaterConfig().getSpecularLightConfig().getSpecularIntensity());
        // TODO webGlFacade.uniform1f(uLightSpecularHardness, uiTerrainWaterTile.getWaterConfig().getSpecularLightConfig().getSpecularHardness());

//        webGlFacade.uniform1f(uTransparency, uiTerrainWaterTile.getWaterConfig().getTransparency());
//        webGlFacade.uniform1f(distortionStrength, uiTerrainWaterTile.getWaterConfig().getDistortionStrength());
//        webGlFacade.uniform1f(normMapDepth, uiTerrainWaterTile.getWaterConfig().getNormMapDepth());
//
//        webGlFacade.uniform1f(animation, uiTerrainWaterTile.getWaterAnimation());

        positions.activate();

//        reflection.overrideScale(uiTerrainWaterTile.getWaterConfig().getReflectionScale());
//        reflection.activate();
//        bumpMap.activate();
//        distortionMap.overrideScale(uiTerrainWaterTile.getWaterConfig().getDistortionScale());
//        distortionMap.activate();

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
    }

}
