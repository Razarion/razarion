package com.btxtech.uiservice.renderer.task.progress;

import com.btxtech.shared.datatypes.shape.VertexContainer;

public class BuildupState extends ProgressState {
    private double[] buildupMatrix;
    private double maxZ;

    public BuildupState(double maxZ, int buildupTextureId) {
        super(buildupTextureId);
        this.maxZ = maxZ;
    }

    public double[] getBuildupMatrix() {
        return buildupMatrix;
    }

    @Override
    public double calculateProgress(double progress) {
        return maxZ * progress;
    }

    @Override
    public void setupAdditional(VertexContainer vertexContainer) {
        buildupMatrix = vertexContainer.getShapeTransform().setupMatrix().toWebGlArray();
    }
}
