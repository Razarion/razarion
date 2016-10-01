package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * Created by Beat
 * 28.09.2016.
 */
public class SelectionEvent {
    public enum Type {
        CLEAR,
        OWN,
        TRAGET
    }
    private Type type;
    private Group selectedGroup;
    private SyncItem targetSelection;

    public SelectionEvent(Type type, Group selectedGroup) {
        this.type = type;
        this.selectedGroup = selectedGroup;
    }

    public SelectionEvent(Type type, SyncItem targetSelection) {
        this.type = type;
        this.targetSelection = targetSelection;
    }

    public Type getType() {
        return type;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public SyncItem getTargetSelection() {
        return targetSelection;
    }
}
