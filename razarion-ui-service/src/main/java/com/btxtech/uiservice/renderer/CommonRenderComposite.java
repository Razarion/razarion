package com.btxtech.uiservice.renderer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 05.09.2016.
 * <p>
 * U: render unit
 * D: render data
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

    public U setRenderUnit(Class<U> clazz) {
        U u = instance.select(clazz).get();
        setRenderUnit(u);
        return u;
    }

    public U setDepthBufferRenderUnit(Class<U> clazz) {
        U u = depthBufferInstance.select(clazz).get();
        setDepthBufferRenderUnit(u);
        return u;
    }

    public void setWireRenderUnit(Class<U> clazz) {
        throw new UnsupportedOperationException("...Noch machen...");
        // setWireRenderUnit(wireInstance.select(clazz).get());
    }

    public U setNormRenderUnit(Class<U> clazz) {
        // Ignore norm tue to startup performance
        return null;
        // U u = normInstance.select(clazz).get();
        // setNormRenderUnit(u);
        // return u;
    }

}
