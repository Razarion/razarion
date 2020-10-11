package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.task.simple.GroundRenderTaskRunner;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

import static com.btxtech.client.renderer.shaders.SkeletonDefines.WORLD_VERTEX_POSITION;

/**
 * Created by Beat
 * 01.05.2015.
 */
@Dependent
public class GroundRenderTask extends AbstractWebGlRenderTask<UiTerrainGroundTile> implements GroundRenderTaskRunner.RenderTask {
    // private Logger logger = Logger.getLogger(ClientGroundRendererUnit.class.getName());
    @Inject
    private GameUiControl gameUiControl;
//    @Inject
//    private InGameQuestVisualizationService inGameQuestVisualizationService;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(UiTerrainGroundTile uiTerrainGroundTile) {
        return new WebGlFacadeConfig(Shaders.INSTANCE.groundCustom())
                .enableTransformation(true)
                .enableReceiveShadow()
                .enableCastShadow()
                .enableOESStandardDerivatives()
                .enableLight();
    }

    @Override
    public void setup(UiTerrainGroundTile uiTerrainGroundTile) {
        setupVec3PositionArray(uiTerrainGroundTile.getGroundPositions());
        setupVec3Array(WebGlFacade.A_VERTEX_NORMAL, Js.uncheckedCast(uiTerrainGroundTile.getGroundNorms()));

        AlarmRaiser.onNull(uiTerrainGroundTile.getGroundConfig(), Alarm.Type.RENDER_GROUND_FAILED, "No GroundConfig in Planet (Id: " + gameUiControl.getPlanetConfig().createObjectNameId());
        setupGroundMaterial(uiTerrainGroundTile.getGroundConfig());

//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    protected void glslVertexCustomDefines(List<String> defines, UiTerrainGroundTile uiTerrainGroundTile) {
        defines.add(WORLD_VERTEX_POSITION);
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, UiTerrainGroundTile uiTerrainGroundTile) {
        defines.add(WORLD_VERTEX_POSITION);
        if (uiTerrainGroundTile.getGroundConfig().getBottomMaterial() != null && uiTerrainGroundTile.getGroundConfig().getSplatting() != null) {
            defines.add("RENDER_GROUND_BOTTOM_TEXTURE");
        }
    }
}
