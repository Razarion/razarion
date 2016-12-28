package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * Created by Beat
 * 28.09.2016.
 */
public class SelectionEvent {
    private boolean dueToNewSelection;

    public enum Type {
        CLEAR,
        OWN,
        TARGET
    }

    private Type type;
    private Group selectedGroup;
    private SyncItem targetSelection;

    public SelectionEvent(Group selectedGroup) {
        type = Type.OWN;
        this.selectedGroup = selectedGroup;
    }

    public SelectionEvent(SyncItem targetSelection) {
        type = Type.TARGET;
        this.targetSelection = targetSelection;
    }

    public SelectionEvent(boolean dueToNewSelection) {
        this.dueToNewSelection = dueToNewSelection;
        type = Type.CLEAR;
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

    public boolean isDueToNewSelection() {
        return dueToNewSelection;
    }
}
