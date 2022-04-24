package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;

@Deprecated
public class WebGlGroundMaterial {
    private WebGlPhongMaterial topMaterial;
    private WebGlPhongMaterial bottomMaterial;
    private WebGlSplatting splatting;

    public WebGlGroundMaterial(WebGlFacade webGlFacade, GroundConfig groundConfig) {
//        AlarmRaiser.onNull(groundConfig.getTopMaterial(), Alarm.Type.RENDER_GROUND_FAILED, "No top material on GroundConfig: ", groundConfig.getId());
//        topMaterial = webGlFacade.createPhongMaterial(groundConfig.getTopMaterial(), "topMaterial");
//        bottomMaterial = webGlFacade.createPhongMaterial(groundConfig.getBottomMaterial(), "bottomMaterial");
//        if (bottomMaterial != null && groundConfig.getSplatting() != null) {
//            splatting = webGlFacade.createSplatting(groundConfig.getSplatting(), "splatting");
//        }

    }

    public void activate() {
        topMaterial.activate();
        if (bottomMaterial != null && splatting != null) {
            bottomMaterial.activate();
            splatting.activate();
        }

    }
}
