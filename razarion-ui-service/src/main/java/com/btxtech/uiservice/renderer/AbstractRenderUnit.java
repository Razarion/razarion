package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import java.util.List;

/**
 * Created by Beat
 * 03.09.2015.
 */
public abstract class AbstractRenderUnit<D> {
    private AbstractRenderComposite<AbstractRenderUnit<D>, D> abstractRenderComposite;
    private int elementCount;

    public abstract void init();

    @Deprecated
    public void setupImages() {

    }

    public abstract void fillBuffers(D d);

    protected abstract void prepareDraw();

    protected void afterDraw() {

    }

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

    public String helperString() {
        return "???";
    }

    // Override in subclasses
    public void dispose() {

    }

    /**
     * Override in subclasses
     * The defines in the vertex shader.
     * "#deine "+ define + "\n"
     *
     * @return define names
     */
    public List<String> getGlslVertexDefines() {
        return null;
    }

    /**
     * Override in subclasses
     * The defines in the fragment shader.
     * "#deine "+ define + "\n"
     *
     * @return define names
     */
    public List<String> getGlslFragmentDefines() {
        return null;
    }
}
