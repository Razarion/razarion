package com.btxtech.uiservice.cdimock.renderer;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ProgressAnimation;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

public abstract class AbstractRenderTaskMock<T> {
    private Logger logger = Logger.getLogger(AbstractRenderTaskMock.class.getName());
    private Function<Long, List<ModelMatrices>> modelMatricesSupplier;

    public void draw(double interpolationFactor) {
        logger.fine("AbstractRenderTaskMock.draw() " + interpolationFactor);
    }

    public void setActive(boolean active) {
        logger.fine("AbstractRenderTaskMock.setActive() " + active);
    }

    public void setModelMatricesSupplier(Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        this.modelMatricesSupplier = modelMatricesSupplier;
    }

    public void dispose() {
        logger.fine("AbstractRenderTaskMock.dispose()");
    }

    public void setShapeTransform(ShapeTransform shapeTransform) {
        logger.fine("AbstractRenderTaskMock.setShapeTransform() " + shapeTransform);
    }

    public void setProgressAnimations(Collection<ProgressAnimation> setupProgressAnimation) {
        logger.fine("AbstractRenderTaskMock.setProgressAnimations() " + setupProgressAnimation);
    }

    public Function<Long, List<ModelMatrices>> getModelMatricesSupplier() {
        return modelMatricesSupplier;
    }
}
