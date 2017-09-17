package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.dto.BoxRegionConfig;

/**
 * Created by Beat
 * on 15.09.2017.
 */
public class BoxRegion {
    private BoxRegionConfig boxRegionConfig;
    private long tickCount;

    public BoxRegion(BoxRegionConfig boxRegionConfig) {
        this.boxRegionConfig = boxRegionConfig;
        setupNextDropTime();
    }

    /**
     * Tick the region
     *
     * @param ttlAmount amount of ticks to subtract from TTL count
     * @return true if drop needed
     */
    public boolean tick(long ttlAmount) {
        tickCount -= ttlAmount;
        return tickCount <= 0;
    }

    public void setupNextDropTime() {
        tickCount = PlanetService.TICKS_PER_SECONDS * Math.round(Math.random() * (boxRegionConfig.getMaxInterval() - boxRegionConfig.getMinInterval()) + boxRegionConfig.getMinInterval());
    }

    public BoxRegionConfig getBoxRegionConfig() {
        return boxRegionConfig;
    }
}
