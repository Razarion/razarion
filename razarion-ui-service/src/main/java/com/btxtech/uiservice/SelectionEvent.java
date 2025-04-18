package com.btxtech.uiservice;

import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;

/**
 * Created by Beat
 * 28.09.2016.
 */
public class SelectionEvent {
    private boolean suppressAudio;

    public enum Type {
        CLEAR,
        OWN,
        OTHER
    }

    private final Type type;
    private Group selectedGroup;
    private SyncItemSimpleDto selectedOther;

    public SelectionEvent(Group selectedGroup, boolean suppressAudio) {
        type = Type.OWN;
        this.selectedGroup = selectedGroup;
        this.suppressAudio = suppressAudio;
    }

    public SelectionEvent(SyncItemSimpleDto selectedOther) {
        type = Type.OTHER;
        this.selectedOther = selectedOther;
    }

    public SelectionEvent(boolean suppressAudio) {
        type = Type.CLEAR;
        this.suppressAudio = suppressAudio;
    }

    public Type getType() {
        return type;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public SyncItemSimpleDto getSelectedOther() {
        return selectedOther;
    }

    public boolean isSuppressAudio() {
        return suppressAudio;
    }
}
