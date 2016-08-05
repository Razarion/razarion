package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.uiservice.item.BaseItemUiService;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Dependent
public class AliveItemTypeVertexContainerCompositeRenderer extends VertexContainerCompositeRenderer {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Instance<AbstractVertexContainerRenderUnit> instance;

    @Override
    protected void initRenderUnits() {
        AbstractVertexContainerRenderUnit renderer = instance.select(VertexContainerRenderUnit.class).get();
        renderer.init(getVertexContainer());
        setRenderUnit(renderer);
    }

    @Override
    protected Collection<ModelMatrices> provideModelMatrices() {
        return baseItemUiService.provideAliveModelMatrices();
    }
}
