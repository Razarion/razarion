package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.ModelMatrices;

import java.util.Collection;

/**
 * Created by Beat
 * 09.08.2016.
 */
@Deprecated
public interface ModelMatricesProvider {
    Collection<ModelMatrices> provideModelMatrices();
}
