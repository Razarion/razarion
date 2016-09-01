package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.google.gwt.user.client.ui.Composite;

/**
 * Created by Beat
 * 23.08.2016.
 */
public abstract class AbstractPropertyPanel<T extends ObjectNameIdProvider> extends Composite {
    public abstract T getConfigObject();

    public abstract void init(T t);
}
