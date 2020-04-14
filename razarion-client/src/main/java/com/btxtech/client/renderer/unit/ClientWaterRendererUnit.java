package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.annotation.PostConstruct;
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
    private WebGLUniformLocation uLightDirection;
    private WebGLUniformLocation uLightSpecularIntensity;
    private WebGLUniformLocation uLightSpecularHardness;
    private WebGlUniformTexture reflection;
    private WebGlUniformTexture bumpMap;
    private WebGlUniformTexture distortionMap;
    private WebGLUniformLocation uTransparency;
    private WebGLUniformLocation distortionStrength;
    private WebGLUniformLocation normMapDepth;
    private WebGLUniformLocation animation;
    private WebGlUniformTexture terrainMarkerTexture;
    private WebGLUniformLocation terrainMarker2DPoints;
    private WebGLUniformLocation terrainMarkerAnimation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        uLightDirection = webGlFacade.getUniformLocation("uLightDirection");
        uLightSpecularIntensity = webGlFacade.getUniformLocation("uLightSpecularIntensity");
        uLightSpecularHardness = webGlFacade.getUniformLocation("uLightSpecularHardness");
        uTransparency = webGlFacade.getUniformLocation("uTransparency");
        distortionStrength = webGlFacade.getUniformLocation("uDistortionStrength");
        normMapDepth = webGlFacade.getUniformLocation("uNormMapDepth");
        animation = webGlFacade.getUniformLocation("animation");
        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillInternalBuffers(UiTerrainWaterTile uiTerrainWaterTile) {
        positions.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainWaterTile.getTerrainWaterTile().getVertices()));
        reflection = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getReflectionId(), "uReflection", "uReflectionScale", uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        bumpMap = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getNormMapId(), "uNormMap");
        distortionMap = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getDistortionId(), "uDistortionMap", "uDistortionScale", uiTerrainWaterTile.getWaterConfig().getDistortionScale());
    }

    @Override
    public void draw(UiTerrainWaterTile uiTerrainWaterTile) {
        webGlFacade.useProgram();

        webGlFacade.uniform3f(uLightDirection, webGlFacade.getVisualUiService().getLightDirection());
        // TODO webGlFacade.uniform1f(uLightSpecularIntensity, uiTerrainWaterTile.getWaterConfig().getSpecularLightConfig().getSpecularIntensity());
        // TODO webGlFacade.uniform1f(uLightSpecularHardness, uiTerrainWaterTile.getWaterConfig().getSpecularLightConfig().getSpecularHardness());

        webGlFacade.uniform1f(uTransparency, uiTerrainWaterTile.getWaterConfig().getTransparency());
        webGlFacade.uniform1f(distortionStrength, uiTerrainWaterTile.getWaterConfig().getDistortionStrength());
        webGlFacade.uniform1f(normMapDepth, uiTerrainWaterTile.getWaterConfig().getNormMapDepth());

        webGlFacade.uniform1f(animation, uiTerrainWaterTile.getWaterAnimation());

        positions.activate();

        reflection.overrideScale(uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        reflection.activate();
        bumpMap.activate();
        distortionMap.overrideScale(uiTerrainWaterTile.getWaterConfig().getDistortionScale());
        distortionMap.activate();

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
    public void dispose() {
        positions.deleteBuffer();
    }

}
