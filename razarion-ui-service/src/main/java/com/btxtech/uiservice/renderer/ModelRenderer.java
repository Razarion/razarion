package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.context.Dependent;
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
public class ModelRenderer<T> {
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

    @Deprecated
    public void add(RenderUnitControl renderUnitControl, AbstractRenderComposite abstractRenderCompositeRenderers) {
        // TODO Backward compatibility remove this method
    }

    @Deprecated
    public <U extends AbstractRenderUnit<D>, D> CommonRenderComposite<U, D> create() {
//        CommonRenderComposite<U, D> commonRenderComposite = (CommonRenderComposite<U, D>) instanceCommonRenderComposite.get();
//        commonRenderComposite.setModelRenderer(this);
//        return commonRenderComposite;
        return null;
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
        // TODO renderSubTasks.getSave(renderUnitControl).forEach(webGlProgram -> webGlProgram.draw(modelMatrices, interpolationFactor));
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void dispose() {
        // TODO renderSubTasks.getAll().forEach(RenderTask::dispose);
    }
}
