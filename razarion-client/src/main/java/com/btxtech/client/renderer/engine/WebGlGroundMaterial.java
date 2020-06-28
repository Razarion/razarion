package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.control.GameUiControl;

public class WebGlGroundMaterial {
    private WebGlFacade webGlFacade;
    private GameUiControl gameUiControl;
    private WebGlPhongMaterial topMaterial;
    private WebGlPhongMaterial bottomMaterial;
    private WebGlSplatting splatting;

    public WebGlGroundMaterial(WebGlFacade webGlFacade, GameUiControl gameUiControl) {
        this.webGlFacade = webGlFacade;
        this.gameUiControl = gameUiControl;
    }

    public void init(GroundConfig groundConfig) {
        AlarmRaiser.onNull(groundConfig, Alarm.Type.RENDER_GROUND_FAILED, "No GroundConfig in UiTerrainGroundTile: ", gameUiControl.getPlanetConfig().getId());
        AlarmRaiser.onNull(groundConfig.getTopMaterial(), Alarm.Type.RENDER_GROUND_FAILED, "No top material on GroundConfig: ", groundConfig.getId());
        topMaterial = webGlFacade.createPhongMaterial(groundConfig.getTopMaterial(), "topMaterial");
        bottomMaterial = webGlFacade.createPhongMaterial(groundConfig.getBottomMaterial(), "bottomMaterial");
        if (bottomMaterial != null) {
            splatting = webGlFacade.createSplatting(groundConfig.getSplatting(), "splatting");
        }

    }

    public void activate() {
        topMaterial.activate();
        if (bottomMaterial != null && splatting != null) {
            bottomMaterial.activate();
            splatting.activate();
        }

    }
}
