package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.LightUniforms;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.ShallowWaterConfig;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

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
    private Vec2Float32ArrayShaderAttribute uv;
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
    private WebGLUniformLocation bumpDistortionAnimation;
    private LightUniforms lightUniforms;
    private WebGlUniformTexture shallowWater;
    private WebGLUniformLocation shallowWaterScale;
    private WebGlUniformTexture shallowDistortionMap;
    private WebGLUniformLocation shallowDistortionStrength;
    private WebGLUniformLocation shallowAnimation;
    private WebGlUniformTexture waterStencil;

//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    public void init() {
        webGlFacade.enableOESStandardDerivatives();
        webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        shininess = webGlFacade.getUniformLocation("uShininess");
        specularStrength = webGlFacade.getUniformLocation("uSpecularStrength");
        reflectionScale = webGlFacade.getUniformLocation("uReflectionScale");
        transparency = webGlFacade.getUniformLocation("uTransparency");
        fresnelOffset = webGlFacade.getUniformLocation("uFresnelOffset");
        fresnelDelta = webGlFacade.getUniformLocation("uFresnelDelta");
        bumpMapDepth = webGlFacade.getUniformLocation("uBumpMapDepth");
        distortionStrength = webGlFacade.getUniformLocation("uDistortionStrength");
        bumpDistortionScale = webGlFacade.getUniformLocation("uBumpDistortionScale");
        bumpDistortionAnimation = webGlFacade.getUniformLocation("uBumpDistortionAnimation");
        lightUniforms = new LightUniforms(webGlFacade);

        reflection = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getReflectionId(), "uReflection");
        bumpMap = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getBumpMapId(), "uBumpMap");
        distortionMap = webGlFacade.createSaveWebGLTexture(getRenderData().getWaterConfig().getDistortionId(), "uDistortionMap");

//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    protected void fillInternalBuffers(UiTerrainWaterTile uiTerrainWaterTile) {
        if (hasShallowWater()) {
            uv = webGlFacade.createVec2Float32ArrayShaderAttribute("uv");
            uv.fillFloat32Array(Js.uncheckedCast(uiTerrainWaterTile.getUvs()));
            ShallowWaterConfig shallowWaterConfig = getRenderData().getShallowWaterConfig();
            shallowWater = webGlFacade.createSaveWebGLTexture(shallowWaterConfig.getTextureId(), "uShallowWater");
            shallowWaterScale = webGlFacade.getUniformLocation("uShallowWaterScale");
            shallowDistortionMap = webGlFacade.createSaveWebGLTexture(shallowWaterConfig.getDistortionId(), "uShallowDistortionMap");
            shallowDistortionStrength = webGlFacade.getUniformLocation("uShallowDistortionStrength");
            shallowAnimation = webGlFacade.getUniformLocation("uShallowAnimation");
            waterStencil = webGlFacade.createSaveWebGLTexture(shallowWaterConfig.getStencilId(), "uWaterStencil");
        }

        Float32Array waterPositions = Js.uncheckedCast(uiTerrainWaterTile.getPositions());
        positions.fillFloat32Array(waterPositions);
        setElementCount((int) (waterPositions.length / Vertex.getComponentsPerVertex()));
    }

    @Override
    public void draw(UiTerrainWaterTile uiTerrainWaterTile) {
//        if(webGlFacade.canBeSkipped()) {
//            return;
//        }
        webGlFacade.useProgram();
        // webGlFacade.setTransformationUniforms();

        positions.activate();
        if (uv != null) {
            uv.activate();
            shallowWater.activate();
            ShallowWaterConfig shallowWaterConfig = getRenderData().getShallowWaterConfig();
            webGlFacade.uniform1f(shallowWaterScale, shallowWaterConfig.getScale());
            shallowDistortionMap.activate();
            webGlFacade.uniform1f(shallowDistortionStrength, shallowWaterConfig.getDistortionStrength());
            webGlFacade.uniform1f(shallowAnimation, uiTerrainWaterTile.getShallowWaterAnimation());
            waterStencil.activate();
        }

        lightUniforms.setLightUniforms(webGlFacade);

        webGlFacade.uniform1f(shininess, uiTerrainWaterTile.getWaterConfig().getShininess());
        webGlFacade.uniform1f(specularStrength, uiTerrainWaterTile.getWaterConfig().getSpecularStrength());
        webGlFacade.uniform1f(reflectionScale, uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        webGlFacade.uniform1f(transparency, uiTerrainWaterTile.getWaterConfig().getTransparency());
        webGlFacade.uniform1f(fresnelOffset, uiTerrainWaterTile.getWaterConfig().getFresnelOffset());
        webGlFacade.uniform1f(fresnelDelta, uiTerrainWaterTile.getWaterConfig().getFresnelDelta());
        webGlFacade.uniform1f(bumpMapDepth, uiTerrainWaterTile.getWaterConfig().getBumpMapDepth());
        webGlFacade.uniform1f(distortionStrength, uiTerrainWaterTile.getWaterConfig().getDistortionStrength());
        webGlFacade.uniform1f(bumpDistortionScale, uiTerrainWaterTile.getWaterConfig().getBumpDistortionScale());
        webGlFacade.uniform1f(bumpDistortionAnimation, uiTerrainWaterTile.getWaterAnimation());

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
    public List<String> getGlslVertexDefines() {
        if (hasShallowWater()) {
            return Collections.singletonList("RENDER_SHALLOW_WATER");
        }
        return null;
    }

    @Override
    public List<String> getGlslFragmentDefines() {
        if (hasShallowWater()) {
            return Collections.singletonList("RENDER_SHALLOW_WATER");
        }
        return null;
    }

    @Override
    public void dispose() {
        positions.deleteBuffer();
    }

    private boolean hasShallowWater() {
        // Cast to Float32Array needed due to GWT problems (if not working)
        Float32Array uvFloat32Array = Js.uncheckedCast(getRenderData().getUvs());
        return uvFloat32Array != null && getRenderData().getShallowWaterConfig() != null;
    }

}
