package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;
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
    // TODO @Inject
    // TODO private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Vec3Float32ArrayShaderAttribute positions;
    private WebGLUniformLocation shininess;
    private WebGLUniformLocation specularStrength;
    private WebGLUniformLocation reflectionScale;
    private WebGlUniformTexture reflection;
    private WebGLUniformLocation transparency;
    private WebGLUniformLocation fresnelOffset;
    private WebGLUniformLocation fresnelDelta;
    private WebGlUniformTexture bumpMap;
    private WebGLUniformLocation bumpMapDepth;
    private WebGlUniformTexture distortionMap;
    private WebGLUniformLocation distortionStrength;
    private WebGLUniformLocation bumpDistortionScale;
    private WebGLUniformLocation bumpDistortionAnimatioon;

//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        shininess = webGlFacade.getUniformLocation("uShininess");
        specularStrength = webGlFacade.getUniformLocation("uSpecularStrength");
        reflectionScale = webGlFacade.getUniformLocation("uSpecularStrength");
        transparency = webGlFacade.getUniformLocation("uTransparency");
        fresnelOffset = webGlFacade.getUniformLocation("uFresnelOffset");
        fresnelDelta = webGlFacade.getUniformLocation("uFresnelDelta");
        bumpMapDepth = webGlFacade.getUniformLocation("uBumpMapDepth");
        distortionStrength = webGlFacade.getUniformLocation("uDistortionStrength");
        bumpDistortionScale = webGlFacade.getUniformLocation("uBumpMapDepth");
        bumpDistortionAnimatioon = webGlFacade.getUniformLocation("uBumpMapDepth");


//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    public void setupImages() {
        reflection = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getReflectionId(), "uReflection");
        bumpMap = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getBumpMapId(), "uBumpMap");
        distortionMap = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getDistortionId(), "uDistortionMap");
    }

    @Override
    protected void fillInternalBuffers(UiTerrainWaterTile uiTerrainWaterTile) {
        Float32Array waterPositions = Js.uncheckedCast(uiTerrainWaterTile.getPositions());
        positions.fillFloat32Array(waterPositions);
        setElementCount((int) (waterPositions.length / Vertex.getComponentsPerVertex()));
    }

    @Override
    public void draw(UiTerrainWaterTile uiTerrainWaterTile) {
        webGlFacade.useProgram();

        positions.activate();

        webGlFacade.uniform1f(shininess, uiTerrainWaterTile.getWaterConfig().getShininess());
        webGlFacade.uniform1f(specularStrength, uiTerrainWaterTile.getWaterConfig().getSpecularStrength());
        webGlFacade.uniform1f(reflectionScale, uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        webGlFacade.uniform1f(transparency, uiTerrainWaterTile.getWaterConfig().getTransparency());
        webGlFacade.uniform1f(fresnelOffset, uiTerrainWaterTile.getWaterConfig().getFresnelOffset());
        webGlFacade.uniform1f(fresnelDelta, uiTerrainWaterTile.getWaterConfig().getFresnelDelta());
        webGlFacade.uniform1f(bumpMapDepth, uiTerrainWaterTile.getWaterConfig().getBumpMapDepth());
        webGlFacade.uniform1f(distortionStrength, uiTerrainWaterTile.getWaterConfig().getBumpDistortionScale());
        webGlFacade.uniform1f(bumpDistortionScale, uiTerrainWaterTile.getWaterConfig().getBumpDistortionScale());
        webGlFacade.uniform1f(bumpDistortionAnimatioon, uiTerrainWaterTile.getWaterAnimation());

        reflection.activate();
        bumpMap.activate();
        distortionMap.activate();

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
