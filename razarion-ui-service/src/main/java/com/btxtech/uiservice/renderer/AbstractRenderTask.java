package com.btxtech.uiservice.renderer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 31.08.2016.
 */
public abstract class AbstractRenderTask {
    @Inject
    private RenderService renderService;
    private List<CompositeRenderer> compositeRenderers = new ArrayList<>();

    /**
     * Override in sub classes
     *
     * @return if the task should be rendered
     */
    protected boolean isActive() {
        return true;
    }

    protected void add(CompositeRenderer compositeRenderers) {
        this.compositeRenderers.add(compositeRenderers);
    }

    protected void removeAll(Collection<CompositeRenderer> compositeRenderers) {
        this.compositeRenderers.removeAll(compositeRenderers);
    }

    protected List<CompositeRenderer> getAll() {
        return compositeRenderers;
    }

    protected boolean isShowNorm() {
        return renderService.isShowNorm();
    }

    public void drawDepthBuffer() {
        if (isActive()) {
            compositeRenderers.forEach(CompositeRenderer::drawDepthBuffer);
        }
    }

    public void draw() {
        if (isActive()) {
            compositeRenderers.forEach(CompositeRenderer::draw);
        }
    }

    public void drawNorm() {
        if (isActive()) {
            compositeRenderers.forEach(CompositeRenderer::drawNorm);
        }
    }

    public void fillBuffers() {
        compositeRenderers.forEach(CompositeRenderer::fillBuffers);
    }

    public void fillNormBuffer() {
        compositeRenderers.forEach(CompositeRenderer::fillNormBuffer);
    }
}
