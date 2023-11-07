package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@JsType
@ApplicationScoped
public class AssetService {
    private MeshContainer[] meshContainers;
    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        if (gameUiControlInitEvent.getColdGameUiContext().getMeshContainers() != null) {
            meshContainers = gameUiControlInitEvent.getColdGameUiContext().getMeshContainers().toArray(new MeshContainer[0]);
        }
    }

    public MeshContainer[] getMeshContainers() {
        return meshContainers;
    }

}
