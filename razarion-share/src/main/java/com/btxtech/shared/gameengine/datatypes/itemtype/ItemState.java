package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Deprecated
@Portable
public enum ItemState {
    BEAM_UP,
    IDLE,
    MOVING,
    BUILDING,
    FABRICATE
}
