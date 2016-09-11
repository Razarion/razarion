package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.ModelMatrices;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

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
    private MapList<RenderUnitControl, C> abstractRenderComposites = new MapList<>();
    private List<ModelMatrices> modelMatrices;
    private Supplier<List<ModelMatrices>> modelMatricesSupplier;
    private T model;

    public void init(T model, Supplier<List<ModelMatrices>> modelMatricesProvider) {
        this.model = model;
        this.modelMatricesSupplier = modelMatricesProvider;
    }

    public T getModel() {
        return model;
    }

    public void add(RenderUnitControl renderUnitControl, C abstractRenderCompositeRenderers) {
        this.abstractRenderComposites.put(renderUnitControl, abstractRenderCompositeRenderers);
    }

    public <U extends AbstractRenderUnit<D>, D> CommonRenderComposite<U, D> create() {
        return (CommonRenderComposite) instanceCommonRenderComposite.get();
    }

    public C create(Class<C> clazz) {
        return instance.select(clazz).get();
    }

    public void setupModelMatrices() {
        if (modelMatricesSupplier != null) {
            modelMatrices = modelMatricesSupplier.get();
        } else {
            modelMatrices = null;
        }
    }

    public void draw(RenderUnitControl renderUnitControl) {
        abstractRenderComposites.getSave(renderUnitControl).forEach(abstractRenderComposite -> abstractRenderComposite.draw(modelMatrices));
    }

    public void drawDepthBuffer() {
        abstractRenderComposites.getAll().forEach(abstractRenderComposite -> abstractRenderComposite.drawDepthBuffer(modelMatrices));
    }

    public void drawNorm() {
        abstractRenderComposites.getAll().forEach(abstractRenderComposite -> abstractRenderComposite.drawNorm(modelMatrices));
    }

    public void fillBuffers() {
        abstractRenderComposites.getAll().forEach(AbstractRenderComposite::fillBuffers);
    }

    public void fillNormBuffer() {
        abstractRenderComposites.getAll().forEach(AbstractRenderComposite::fillNormBuffer);
    }
}
