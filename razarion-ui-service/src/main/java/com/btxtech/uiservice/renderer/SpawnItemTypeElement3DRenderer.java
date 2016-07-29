package com.btxtech.uiservice.renderer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Dependent
public class SpawnItemTypeElement3DRenderer extends Element3DRenderer {
    @Inject
    private Instance<SpawnItemTypeVertexContainerCompositeRenderer> instance;

    @Override
    protected VertexContainerCompositeRenderer createVertexContainerRenderer() {
        return instance.get();
    }

}
