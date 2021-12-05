package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AssetService {
    private final Map<Integer, MeshContainer> meshContainers = new HashMap<>();

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        meshContainers.clear();
        gameUiControlInitEvent.getColdGameUiContext().getMeshContainers().forEach(meshContainer -> meshContainers.put(meshContainer.getId(), meshContainer));
    }

    public MeshContainer getMeshContainer(int meshContainerId) {
        MeshContainer meshContainer = meshContainers.get(meshContainerId);
        if (meshContainer == null) {
            throw new IllegalArgumentException("No MeshContainer for id: " + meshContainerId);
        }
        return meshContainer;
    }
}
