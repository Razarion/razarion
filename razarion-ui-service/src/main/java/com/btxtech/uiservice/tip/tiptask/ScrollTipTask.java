package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 16.12.2016.
 */
@Dependent
public class ScrollTipTask extends AbstractTipTask {
    private DecimalPosition terrainPositionHint;

    public void init(DecimalPosition terrainPositionHint) {
        this.terrainPositionHint = terrainPositionHint;
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    protected void internalStart() {

    }

    @Override
    protected void internalCleanup() {

    }

    @Override
    public InGameDirectionVisualization createInGameDirectionVisualization() {
        return new InGameDirectionVisualization(getGameTipVisualConfig().getDirectionShape3DId(), terrainPositionHint);
    }
}
