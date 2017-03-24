package com.btxtech.shared.datatypes.shape;

/**
 * Created by Beat
 * 16.10.2016.
 */
public enum AnimationTrigger {
    // Only used for spawn(beam) animation. May be used for buildup and demolition in the future.
    @Deprecated
    ITEM_PROGRESS,
    // Was used in Clips. Now replaced with particles
    @Deprecated
    SINGLE_RUN,
    CONTINUES
}
