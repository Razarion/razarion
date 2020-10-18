package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.gameengine.datatypes.config.ShallowWaterConfig;
import com.btxtech.uiservice.renderer.task.simple.WaterRenderTaskRunner;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;
import elemental2.core.Float32Array;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import java.util.List;

import static com.btxtech.client.renderer.engine.UniformLocation.Type.F;
import static com.btxtech.client.renderer.shaders.Shaders.SHADERS;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.FIX_PERPENDICULAR_NORMAL;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.UV;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.WORLD_VERTEX_POSITION;
import static com.btxtech.client.renderer.webgl.WebGlFacadeConfig.Blend.SOURCE_ALPHA;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WaterRenderTask extends AbstractWebGlRenderTask<UiTerrainWaterTile> implements WaterRenderTaskRunner.RenderTask {
    // private Logger logger = Logger.getLogger(ClientWaterRendererUnit.class.getName());
    // TODO @Inject
    // TODO private InGameQuestVisualizationService inGameQuestVisualizationService;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;


    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(UiTerrainWaterTile uiTerrainWaterTile) {
        return new WebGlFacadeConfig(SHADERS.customWater())
                .enableTransformation(true)
                .enableOESStandardDerivatives()
                .blend(SOURCE_ALPHA)
                .enableLight();
    }

    @Override
    protected void setup(UiTerrainWaterTile uiTerrainWaterTile) {
        setupVec3PositionArray(uiTerrainWaterTile.getPositions());

        setupUniform("uShininess", F, () -> uiTerrainWaterTile.getWaterConfig().getShininess());
        setupUniform("uSpecularStrength", F, () -> uiTerrainWaterTile.getWaterConfig().getSpecularStrength());
        setupUniform("uReflectionScale", F, () -> uiTerrainWaterTile.getWaterConfig().getReflectionScale());
        setupUniform("uTransparency", F, () -> uiTerrainWaterTile.getWaterConfig().getTransparency());
        setupUniform("uFresnelOffset", F, () -> uiTerrainWaterTile.getWaterConfig().getFresnelOffset());
        setupUniform("uFresnelDelta", F, () -> uiTerrainWaterTile.getWaterConfig().getFresnelDelta());
        setupUniform("uBumpMapDepth", F, () -> uiTerrainWaterTile.getWaterConfig().getBumpMapDepth());
        setupUniform("uDistortionStrength", F, () -> uiTerrainWaterTile.getWaterConfig().getDistortionStrength());
        setupUniform("uBumpDistortionScale", F, () -> uiTerrainWaterTile.getWaterConfig().getBumpDistortionScale());
        setupUniform("uBumpDistortionAnimation", F, uiTerrainWaterTile::getWaterAnimation);

        createWebGLTexture("uReflection", uiTerrainWaterTile.getWaterConfig().getReflectionId());
        createWebGLTexture("uBumpMap", uiTerrainWaterTile.getWaterConfig().getBumpMapId());
        createWebGLTexture("uDistortionMap", uiTerrainWaterTile.getWaterConfig().getDistortionId());

        if (hasShallowWater(uiTerrainWaterTile)) {
            setupVec2Array("uv", uiTerrainWaterTile.getUvs());

            ShallowWaterConfig shallowWaterConfig = uiTerrainWaterTile.getShallowWaterConfig();
            setupUniform("uShallowWaterScale", F, shallowWaterConfig::getScale);
            setupUniform("uShallowDistortionStrength", F, shallowWaterConfig::getDistortionStrength);
            setupUniform("uShallowAnimation", F, uiTerrainWaterTile::getShallowWaterAnimation);
            createWebGLTexture("uShallowWater", shallowWaterConfig.getTextureId());
            createWebGLTexture("uShallowDistortionMap", shallowWaterConfig.getDistortionId());
            createWebGLTexture("uWaterStencil", shallowWaterConfig.getStencilId());
        }
    }

    @Override
    protected void glslVertexCustomDefines(List<String> defines, UiTerrainWaterTile uiTerrainWaterTile) {
        defines.add(FIX_PERPENDICULAR_NORMAL);
        defines.add(WORLD_VERTEX_POSITION);
        if (hasShallowWater(uiTerrainWaterTile)) {
            defines.add(UV);
            defines.add("RENDER_SHALLOW_WATER");
        }
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, UiTerrainWaterTile uiTerrainWaterTile) {
        defines.add(WORLD_VERTEX_POSITION);
        if (hasShallowWater(uiTerrainWaterTile)) {
            defines.add(UV);
            defines.add("RENDER_SHALLOW_WATER");
        }
    }

    private boolean hasShallowWater(UiTerrainWaterTile uiTerrainWaterTile) {
        // Cast to Float32Array needed due to GWT problems (if not working)
        Float32Array uvFloat32Array = Js.uncheckedCast(uiTerrainWaterTile.getUvs());
        return uvFloat32Array != null && uiTerrainWaterTile.getShallowWaterConfig() != null;
    }

}
