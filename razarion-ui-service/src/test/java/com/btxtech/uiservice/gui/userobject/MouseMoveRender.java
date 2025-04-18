package com.btxtech.uiservice.gui.userobject;

import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.scene.canvas.GraphicsContext;

/**
 * Created by Beat
 * on 15.12.2017.
 */
public abstract class MouseMoveRender {

    private DecimalPosition decimalPosition;

    public void onMouseMove(DecimalPosition position) {
        this.decimalPosition = position;
    }

    public void render(GraphicsContext gc) {
        renderMouse(gc, decimalPosition);
    }

    protected abstract void renderMouse(GraphicsContext gc, DecimalPosition position);
}
