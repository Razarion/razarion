package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Deprecated
public interface WebGlRenderTask<T> {
    void init(T t);

    void draw(double interpolationFactor);

    void setActive(boolean active);

    void setModelMatricesSupplier(Function<Long, List<ModelMatrices>> modelMatricesSupplier);

    void dispose();

    void setShapeTransform(ShapeTransform shapeTransform);

    void setProgressAnimations(Collection<ProgressAnimation> setupProgressAnimation);
}
