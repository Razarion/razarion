package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.datatypes.terrain.SlopeUi;

import java.util.List;

/**
 * Created by Beat
 * 03.09.2015.
 */
public abstract class AbstractRenderUnit<D> {
    private AbstractRenderComposite<AbstractRenderUnit<D>, D> abstractRenderComposite;
    private int elementCount;

    @Deprecated
    public abstract void setupImages();

    public abstract void fillBuffers(D d);

    protected abstract void prepareDraw();

    protected abstract void draw(ModelMatrices modelMatrices);

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

    protected void setElementCount(SlopeUi slopeUi) {
        elementCount = slopeUi.getElementCount();
    }

    protected void setElementCount(GroundUi groundUi) {
        elementCount = groundUi.getElementCount();
    }

    protected void setElementCount(WaterUi waterUi) {
        elementCount = waterUi.getElementCount();
    }

    protected void setElementCount(List<Vertex> vertices) {
        elementCount = vertices.size();
    }

    public int getElementCount() {
        return elementCount;
    }

    public void setAbstractRenderComposite(AbstractRenderComposite abstractRenderComposite) {
        this.abstractRenderComposite = abstractRenderComposite;
    }

    public AbstractRenderComposite<AbstractRenderUnit<D>, D> getRenderComposite() {
        return abstractRenderComposite;
    }

    protected D getRenderData() {
        return abstractRenderComposite.getRendererData();
    }

    @Deprecated
    protected int getId() {
        return abstractRenderComposite.getId();
    }

    public String helperString() {
        return "???";
    }
}
