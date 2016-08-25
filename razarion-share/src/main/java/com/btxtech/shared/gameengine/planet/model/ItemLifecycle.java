package com.btxtech.shared.gameengine.planet.model;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 01.08.2016.
 */
@Portable
public enum ItemLifecycle {
    SPAWN,
    ALIVE,
    DEAD;

    public ItemLifecycle getNext() {
        switch (this) {
            case SPAWN:
                return ALIVE;
            case ALIVE:
                return DEAD;
            case DEAD:
                return null;
            default:
                throw new IllegalStateException("Unknown: " + this);
        }
    }
}
