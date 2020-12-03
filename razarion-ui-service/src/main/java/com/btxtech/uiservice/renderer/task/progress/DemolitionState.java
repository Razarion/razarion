package com.btxtech.uiservice.renderer.task.progress;

public class DemolitionState extends ProgressState {
    public DemolitionState(int buildupTextureId) {
        super(buildupTextureId);
    }

    @Override
    public double calculateProgress(double progress) {
        return progress;
    }
}
