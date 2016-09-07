package com.btxtech.uiservice.renderer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 05.09.2016.
 *
 * U: render unit
 * D: render data
 *
 */
@Dependent
public class CommonRenderComposite<U extends AbstractRenderUnit<D>, D> extends AbstractRenderComposite<U, D> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractRenderUnit> instance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DepthBufferRenderer
    private Instance<AbstractRenderUnit> depthBufferInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @NormRenderer
    private Instance<AbstractRenderUnit> normInstance;

    public void setRenderUnit(Class<U> clazz) {
        setRenderUnit(instance.select(clazz).get());
    }

    public void setDepthBufferRenderUnit(Class<U> clazz) {
        setDepthBufferRenderUnit(depthBufferInstance.select(clazz).get());
    }

    public void setWireRenderUnit(Class<U> clazz) {
        throw new UnsupportedOperationException("...Noch machen...");
        // setWireRenderUnit(wireInstance.select(clazz).get());
    }

    public void setNormRenderUnit(Class<U> clazz) {
        setNormRenderUnit(normInstance.select(clazz).get());
    }

}
