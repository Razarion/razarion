package com.btxtech.client;

import elemental.events.KeyboardEvent;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class TerrainKeyDownEvent {
    private KeyboardEvent keyboardEvent;

    public TerrainKeyDownEvent(KeyboardEvent keyboardEvent) {
        this.keyboardEvent = keyboardEvent;
    }

    public KeyboardEvent getKeyboardEvent() {
        return keyboardEvent;
    }
}
