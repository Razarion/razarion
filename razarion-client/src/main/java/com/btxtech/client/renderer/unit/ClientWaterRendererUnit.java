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
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

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
    private LightUniforms lightUniforms;
    private WebGlUniformTexture reflection;
    private WebGlUniformTexture bumpMap;
    private WebGlUniformTexture distortionMap;
    private WebGLUniformLocation uWaterColor;
    private WebGLUniformLocation uTransparency;
    private WebGLUniformLocation uBmDepth;
    private WebGLUniformLocation distortionStrength;
    private WebGLUniformLocation animation;
    private WebGLUniformLocation animation2;
    private WebGlUniformTexture terrainMarkerTexture;
    private WebGLUniformLocation terrainMarker2DPoints;
    private WebGLUniformLocation terrainMarkerAnimation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        lightUniforms = new LightUniforms(null, webGlFacade);
        uWaterColor = webGlFacade.getUniformLocation("uWaterColor");
        uTransparency = webGlFacade.getUniformLocation("uTransparency");
        uBmDepth = webGlFacade.getUniformLocation("uBmDepth");
        distortionStrength = webGlFacade.getUniformLocation("uDistortionStrength");
        animation = webGlFacade.getUniformLocation("animation");
        animation2 = webGlFacade.getUniformLocation("animation2");
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
        bumpMap = webGlFacade.createWebGLBumpMapTexture(uiTerrainWaterTile.getWaterConfig().getBmId(), "uBm", "uBmScale", uiTerrainWaterTile.getWaterConfig().getBmScale(), "uBmOnePixel");
        distortionMap = webGlFacade.createWebGLTexture(uiTerrainWaterTile.getWaterConfig().getDistortionId(), "uDistortionMap", "uDistortionScale", uiTerrainWaterTile.getWaterConfig().getDistortionScale());
    }

    @Override
    public void draw(UiTerrainWaterTile uiTerrainWaterTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(uiTerrainWaterTile.getWaterConfig().getSpecularLightConfig(), webGlFacade);

        webGlFacade.uniform3fNoAlpha(uWaterColor, uiTerrainWaterTile.getWaterConfig().getColor());
        webGlFacade.uniform1f(uTransparency, uiTerrainWaterTile.getWaterConfig().getTransparency());
        webGlFacade.uniform1f(uBmDepth, uiTerrainWaterTile.getWaterConfig().getBmDepth());
        webGlFacade.uniform1f(distortionStrength, uiTerrainWaterTile.getWaterConfig().getDistortionStrength());

    webGlFacade.uniform1f(animation, uiTerrainWaterTile.getWaterAnimation());
        webGlFacade.uniform1f(animation2, uiTerrainWaterTile.getWaterAnimation2());

        positions.activate();

        reflection.overrideScale(uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        reflection.activate();
        bumpMap.overrideScale(uiTerrainWaterTile.getWaterConfig().getBmScale());
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
