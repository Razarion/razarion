package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.uiservice.item.SpawnItemUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.07.2016.
 */
@Dependent
public class DevToolsSpawnItemUnitRenderer extends DevToolsAbstractItemUnitRenderer {
    @Inject
    private SpawnItemUiService spawnItemUiService;

    @Override
    protected VertexContainer getVertexContainer() {
        return spawnItemUiService.getSpawnItemTypeVertexContainer(getId());
    }
}
