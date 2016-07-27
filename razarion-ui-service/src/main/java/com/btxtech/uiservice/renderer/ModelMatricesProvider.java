package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;

import java.util.Collection;

/**
 * Created by Beat
 * 22.07.2016.
 */
public interface ModelMatricesProvider {
    Collection<ModelMatrices> provideModelMatrices(int id);
}
