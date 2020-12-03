package com.btxtech.uiservice.renderer.task.progress;

import com.btxtech.shared.datatypes.shape.VertexContainer;

public abstract class ProgressState {
    private int buildupTextureId;

    public ProgressState(int buildupTextureId) {
        this.buildupTextureId = buildupTextureId;
    }

    public int getBuildupTextureId() {
        return buildupTextureId;
    }

    public abstract double calculateProgress(double progress);

    public void setupAdditional(VertexContainer vertexContainer ) {

    }
}


