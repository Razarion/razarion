package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.uiservice.item.BaseItemUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.07.2016.
 */
@Dependent
public class DevToolsBaseItemUnitRenderer extends DevToolsAbstractItemUnitRenderer {
    @Inject
    private BaseItemUiService baseItemUiService;

    @Override
    protected VertexContainer getVertexContainer() {
        return baseItemUiService.getItemTypeVertexContainer(getId());
    }
}
