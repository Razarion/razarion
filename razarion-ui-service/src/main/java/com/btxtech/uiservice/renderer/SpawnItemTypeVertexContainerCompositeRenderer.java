package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.uiservice.item.SpawnItemUiService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 29.07.2016.
 */
public class SpawnItemTypeVertexContainerCompositeRenderer extends VertexContainerCompositeRenderer {
    @Inject
    private SpawnItemUiService spawnItemUiService;
    @Inject
    private Instance<AbstractVertexContainerRenderUnit> instance;

    @Override
    protected void initRenderUnits() {
        AbstractVertexContainerRenderUnit renderer = instance.select(VertexContainerRenderUnit.class).get();
        renderer.init(getVertexContainer());
        setRenderUnit(renderer);
//        setDepthBufferRenderUnit(instance.select(VertexContainerDepthBufferRenderUnit.class).get());
//        setWireRenderUnit(instance.select(VertexContainerWireRenderUnit.class).get());
//        setWireRenderUnit(instance.select(VertexContainerNormRenderUnit.class).get());
    }

    @Override
    protected Collection<ModelMatrices> provideModelMatrices() {
        SpanItemTypeShape3DRenderer spanItemTypeShape3DRenderer = (SpanItemTypeShape3DRenderer) getElement3DRenderer().getShape3DRenderer();
        return spawnItemUiService.provideModelMatrices(spanItemTypeShape3DRenderer.getSpawnItemType());
    }
}
