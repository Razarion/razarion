package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.function.Function;

/**
 * Created by Beat
 * on 15.12.2017.
 */
public class MouseMoveCallback {
    private Function<DecimalPosition, Object[]> callback;

    public Function<DecimalPosition, Object[]> getCallback() {
        return callback;
    }

    public MouseMoveCallback setCallback(Function<DecimalPosition, Object[]> callback) {
        this.callback = callback;
        return this;
    }

    public Object[] onMouseMove(DecimalPosition position) {
        if (callback != null) {
            return callback.apply(position);
        } else {
            return null;
        }
    }
}
