package com.btxtech.uiservice;

/**
 * Created by Beat
 * on 30.07.2017.
 */
public interface EditorKeyboardListener {
    void onDeleteKeyDown(boolean down);

    void onSpaceKeyDown(boolean down);

    void onInsertKeyDown(boolean down);

    void onShiftKeyDown(boolean down);
}
