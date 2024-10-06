package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.InitializeService;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Singleton;

@JsType
@Singleton
public class AssetService {
    private MeshContainer[] meshContainers;

    @Inject
    public AssetService(InitializeService initializeService) {
        initializeService.receiveColdGameUiContext(coldGameUiContext -> {
            if (coldGameUiContext.getMeshContainers() != null) {
                meshContainers = coldGameUiContext.getMeshContainers().toArray(new MeshContainer[0]);
            }
        });
    }

    public MeshContainer[] getMeshContainers() {
        return meshContainers;
    }

}
