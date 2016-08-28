package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.ModelMatricesProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Dependent
public class VertexContainerCompositeRenderer extends CompositeRenderer {
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractVertexContainerRenderUnit> instance;
    @Inject
    @DepthBufferRenderer
    private Instance<AbstractVertexContainerRenderUnit> depthBufferInstance;
    @Inject
    @NormRenderer
    private Instance<AbstractVertexContainerRenderUnit> normInstance;
    private VertexContainer vertexContainer;
    private Element3DRenderer element3DRenderer;
    private ModelMatricesProvider modelMatricesProvider;

    public void init(VertexContainer vertexContainer, Element3DRenderer element3DRenderer, ModelMatricesProvider modelMatricesProvider) {
        this.vertexContainer = vertexContainer;
        this.element3DRenderer = element3DRenderer;
        this.modelMatricesProvider = modelMatricesProvider;
        AbstractVertexContainerRenderUnit renderer = instance.get();
        renderer.init(getVertexContainer());
        setRenderUnit(renderer);
        AbstractVertexContainerRenderUnit depthBufferRenderer = depthBufferInstance.get();
        depthBufferRenderer.init(getVertexContainer());
        setDepthBufferRenderUnit(depthBufferRenderer);
        AbstractVertexContainerRenderUnit normRenderer = normInstance.get();
        normRenderer.init(getVertexContainer());
        setNormRenderUnit(normRenderer);
    }

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }

    protected void draw(AbstractRenderUnit renderUnit) {
        Collection<ModelMatrices> modelMatrices = modelMatricesProvider.provideModelMatrices();
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        renderUnit.preModelDraw();

        for (ModelMatrices modelMatrix : modelMatrices) {
            modelMatrix = element3DRenderer.mixTransformation(modelMatrix, vertexContainer.getShapeTransform());
            renderUnit.modelDraw(modelMatrix);
        }
    }
}
