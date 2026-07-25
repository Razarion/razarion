package com.btxtech.shared.gameengine.planet;

/**
 * Why a {@link com.btxtech.shared.gameengine.planet.model.SyncBoxItem} was removed. Carried through
 * {@link GameLogicService#onBoxDeleted} to the {@link GameLogicListener} so listeners (e.g. the history
 * log) can distinguish an expired box from a picked-up one.
 */
public enum BoxDeletionReason {
    /** Time to live elapsed (BoxService.tick). */
    EXPIRED,
    /** Picked up by a base. */
    PICKED,
    /** Box regions stopped (planet restart / box-region reload). */
    REGION_STOPPED,
    /** Slave engine mirrored a master removal. */
    SLAVE_REMOVED
}
