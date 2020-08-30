package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.datatypes.ModelMatrices;

import java.util.List;

public interface RenderSubTask<T> {
    void init(T t);

    void draw(List<ModelMatrices> modelMatrices, double interpolationFactor);

    void dispose();
}
