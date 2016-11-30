package com.btxtech.client;

import elemental.events.KeyboardEvent;

/**
 * Created by Beat
 * 14.05.2016.
 */
@Deprecated // Only in editor
public class TerrainKeyDownEvent {
    private KeyboardEvent keyboardEvent;

    public TerrainKeyDownEvent(KeyboardEvent keyboardEvent) {
        this.keyboardEvent = keyboardEvent;
    }

    public int getKeyCode() {
        return keyboardEvent.getKeyCode();
    }

    public KeyboardEvent getKeyboardEvent() {
        return keyboardEvent;
    }
}
