package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.WebGlSlopeSplatting;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.renderer.task.simple.SlopeRenderTaskRunner;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import javax.enterprise.context.Dependent;
import java.util.List;

import static com.btxtech.client.renderer.shaders.SkeletonDefines.UV;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.WORLD_VERTEX_POSITION;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Dependent
public class SlopeRenderTask extends AbstractWebGlRenderTask<UiTerrainSlopeTile> implements SlopeRenderTaskRunner.RenderTask {
    // private static Logger logger = Logger.getLogger(ClientSlopeRendererUnit.class.getName());

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(UiTerrainSlopeTile uiTerrainSlopeTile) {
        return new WebGlFacadeConfig(Shaders.INSTANCE.slopeCustom())
                .enableTransformation(true)
                .enableReceiveShadow()
                .enableCastShadow()
                .enableOESStandardDerivatives()
                .enableLight();
    }

    @Override
    protected void setup(UiTerrainSlopeTile uiTerrainSlopeTile) {
        setupVec3PositionArray(uiTerrainSlopeTile.getSlopeGeometry().getPositions());
        setupVec3Array(WebGlFacade.A_VERTEX_NORMAL, uiTerrainSlopeTile.getSlopeGeometry().getNorms());
        setupVec2Array(WebGlFacade.A_VERTEX_UV, uiTerrainSlopeTile.getSlopeGeometry().getUvs());
        setupVec1Array("slopeFactor", uiTerrainSlopeTile.getSlopeGeometry().getSlopeFactors());

        AlarmRaiser.onNull(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), Alarm.Type.INVALID_SLOPE_CONFIG, "No Material in SlopeConfig: ", uiTerrainSlopeTile.getSlopeConfig().getId());
        setupPhongMaterial(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), "material");

        if (uiTerrainSlopeTile.getGroundConfig() != null) {
            setupGroundMaterial(uiTerrainSlopeTile.getGroundConfig());
            if (uiTerrainSlopeTile.getSlopeSplattingConfig() != null) {
                addActivator(new WebGlSlopeSplatting(getWebGlFacade(), uiTerrainSlopeTile.getSlopeSplattingConfig(), "slopeSplatting"));
            }
        }
    }

    @Override
    protected void glslVertexCustomDefines(List<String> defines, UiTerrainSlopeTile uiTerrainSlopeTile) {
        defines.add(WORLD_VERTEX_POSITION);
        defines.add(UV);
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, UiTerrainSlopeTile uiTerrainSlopeTile) {
        defines.add(WORLD_VERTEX_POSITION);
        defines.add(UV);
        if (uiTerrainSlopeTile.getGroundConfig() != null) {
            defines.add("RENDER_GROUND_TEXTURE");
            if (uiTerrainSlopeTile.getGroundConfig().getBottomMaterial() != null && uiTerrainSlopeTile.getGroundConfig().getSplatting() != null) {
                defines.add("RENDER_GROUND_BOTTOM_TEXTURE");
            }
            if (uiTerrainSlopeTile.getSlopeSplattingConfig() != null) {
                defines.add("RENDER_SPLATTING");
            }
        }
    }
}
