package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Beat
 * 06.09.2016.
 * <p>
 * T: model object (e.g. TerrainObjectConfig)
 * C: Render composite (e.g. VertexContainerRenderComposite)
 * U: render unit (e.g.: AbstractVertexContainerRenderUnit)
 * D: render unit data (e.g.: VertexContainer)
 */
@Dependent
public class ModelRenderer<T, C extends AbstractRenderComposite<U, D>, U extends AbstractRenderUnit<D>, D> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private RenderService renderService;
    @Inject
    private Instance<AbstractRenderComposite> instance;
    @Inject
    private Instance<CommonRenderComposite<?, ?>> instanceCommonRenderComposite;
    private MapList<RenderUnitControl, AbstractRenderComposite> abstractRenderComposites = new MapList<>();
    private List<ModelMatrices> modelMatrices;
    private Function<Long, List<ModelMatrices>> modelMatricesSupplier;
    private T model;
    private boolean hasSomethingToDraw;
    private boolean active = true;

    public void init(T model, Function<Long, List<ModelMatrices>> modelMatricesProvider) {
        this.model = model;
        this.modelMatricesSupplier = modelMatricesProvider;
    }

    public T getModel() {
        return model;
    }

    public void add(RenderUnitControl renderUnitControl, AbstractRenderComposite abstractRenderCompositeRenderers) {
        this.abstractRenderComposites.put(renderUnitControl, abstractRenderCompositeRenderers);
    }

    public <U extends AbstractRenderUnit<D>, D> CommonRenderComposite<U, D> create() {
        CommonRenderComposite<U, D> commonRenderComposite = (CommonRenderComposite<U, D>) instanceCommonRenderComposite.get();
        commonRenderComposite.setModelRenderer(this);
        return commonRenderComposite;
    }

    public C create(Class<C> clazz) {
        C c = instance.select(clazz).get();
        c.setModelRenderer(this);
        return c;
    }

    public void setupModelMatrices(long timeStamp) {
        if (!active) {
            return;
        }

        if (modelMatricesSupplier != null) {
            modelMatrices = modelMatricesSupplier.apply(timeStamp);
            hasSomethingToDraw = modelMatrices != null && !modelMatrices.isEmpty();
        } else {
            modelMatrices = null;
            hasSomethingToDraw = true;
        }
    }

    public void draw(RenderUnitControl renderUnitControl, double interpolationFactor) {
        if (!active || !hasSomethingToDraw) {
            return;
        }
        abstractRenderComposites.getSave(renderUnitControl).forEach(abstractRenderComposite -> abstractRenderComposite.draw(modelMatrices, interpolationFactor));
    }

    public void drawDepthBuffer(double interpolationFactor) {
        if (!active || !hasSomethingToDraw) {
            return;
        }
        abstractRenderComposites.getAll().forEach(abstractRenderComposite -> abstractRenderComposite.drawDepthBuffer(modelMatrices, interpolationFactor));
    }

    public void drawNorm(double interpolationFactor) {
        if (!active || !hasSomethingToDraw) {
            return;
        }
        abstractRenderComposites.getAll().forEach(abstractRenderComposite -> abstractRenderComposite.drawNorm(modelMatrices, interpolationFactor));
    }

    public void fillBuffers() {
        abstractRenderComposites.getAll().forEach(AbstractRenderComposite::fillBuffers);
    }

    public void fillNormBuffer() {
        abstractRenderComposites.getAll().forEach(AbstractRenderComposite::fillNormBuffer);
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
