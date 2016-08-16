package com.btxtech.uiservice.renderer;

import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;

/**
 * Created by Beat
 * 03.09.2015.
 */
public abstract class AbstractRenderUnit {
    private CompositeRenderer compositeRenderer;
    private int elementCount;

    @Deprecated
    public abstract void setupImages();

    public abstract void fillBuffers();

    public void draw() {
        throw new IllegalStateException("AbstractRenderUnit.draw() should be overridden");
    }

    protected void preModelDraw() {
        throw new IllegalStateException("AbstractRenderUnit.preModelDraw() should be overridden");
    }

    protected void modelDraw(ModelMatrices modelMatrices) {
        throw new IllegalStateException("AbstractRenderUnit.modelDraw() should be overridden");
    }

    public boolean hasElements() {
        return elementCount > 0;
    }

    protected void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

    protected void setElementCount(VertexContainer vertexContainer) {
        elementCount = vertexContainer.getVerticesCount();
    }

    protected void setElementCount(VertexList vertexList) {
        elementCount = vertexList.getVerticesCount();
    }

    protected void setElementCount(Mesh mesh) {
        elementCount = mesh.size();
    }

    public int getElementCount() {
        return elementCount;
    }

    public void setCompositeRenderer(CompositeRenderer compositeRenderer) {
        this.compositeRenderer = compositeRenderer;
    }

    @Deprecated
    protected int getId() {
        return compositeRenderer.getId();
    }

    public String helperString() {
        return "???";
    }
}
