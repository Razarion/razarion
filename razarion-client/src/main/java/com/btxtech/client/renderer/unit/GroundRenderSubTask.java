package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 01.05.2015.
 */
@Dependent
public class GroundRenderSubTask extends AbstractRenderSubTask<UiTerrainGroundTile> implements GroundRenderTask.SubTask {
    // private Logger logger = Logger.getLogger(ClientGroundRendererUnit.class.getName());
    @Inject
    private GameUiControl gameUiControl;
//    @Inject
//    private InGameQuestVisualizationService inGameQuestVisualizationService;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

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
    protected WebGlFacadeConfig getWebGlFacadeConfig(UiTerrainGroundTile uiTerrainGroundTile) {
        return new WebGlFacadeConfig(null, Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader())
                .enableTransformation(true)
                .enableReceiveShadow()
                .enableCastShadow()
                .enableOESStandardDerivatives()
                .enablelight()
                .glslFragmentDefines(glslFragmentDefines(uiTerrainGroundTile));
    }

    private List<String> glslFragmentDefines(UiTerrainGroundTile uiTerrainGroundTile) {
        if (uiTerrainGroundTile.getGroundConfig().getBottomMaterial() != null && uiTerrainGroundTile.getGroundConfig().getSplatting() != null) {
            return Collections.singletonList("RENDER_GROUND_BOTTOM_TEXTURE");
        }
        return null;
    }
}
