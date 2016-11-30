package com.btxtech.client;

import elemental.events.KeyboardEvent;

/**
 * Created by Beat
 * 14.05.2016.
 */
@Deprecated // Only in editor
public class TerrainKeyUpEvent {
    private KeyboardEvent keyboardEvent;

    public TerrainKeyUpEvent(KeyboardEvent keyboardEvent) {
        this.keyboardEvent = keyboardEvent;
    }

    public KeyboardEvent getKeyboardEvent() {
        return keyboardEvent;
    }
}
