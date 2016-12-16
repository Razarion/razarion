package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.system.SimpleExecutorService;

/**
 * Created by Beat
 * 16.12.2016.
 */
public abstract class AbstractGuiTipVisualization {
    protected Integer imageId;


    public AbstractGuiTipVisualization(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void stop() {
        // Override in subclasses
    }

    public void start(SimpleExecutorService simpleExecutorService) {
        // Override in subclasses
    }

}
