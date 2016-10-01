/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: beat
 * Date: May 19, 2009
 * Time: 9:09:40 PM
 */
@ApplicationScoped
public class SelectionHandler {
    @Inject
    private CommandService commandService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private Event<SelectionEvent> selectionEventEventTrigger;
    private Group selectedGroup; // Always my property
    private SyncItem selectedTargetSyncItem; // Not my property

    public Group getOwnSelection() {
        return selectedGroup;
    }

    public boolean hasOwnSelection() {
        return selectedGroup != null && !selectedGroup.isEmpty();
    }

    public Collection<SurfaceType> getOwnSelectionSurfaceTypes() {
        if (selectedGroup != null) {
            return selectedGroup.getAllowedSurfaceTypes();
        } else {
            return new HashSet<>();
        }
    }

    public boolean atLeastOneItemTypeAllowed2Attack4Selection(SyncBaseItem syncBaseItem) {
        return selectedGroup == null || selectedGroup.atLeastOneItemTypeAllowed2Attack(syncBaseItem);
    }

    public boolean atLeastOneItemTypeAllowed2FinalizeBuild(SyncBaseItem tobeFinalized) {
        return selectedGroup == null || selectedGroup.atLeastOneItemTypeAllowed2FinalizeBuild(tobeFinalized);
    }

    public void setTargetSelected(SyncItem target) {
        if (selectedGroup != null) {
            if (selectedGroup.canAttack() && target instanceof SyncBaseItem) {
                commandService.attack(selectedGroup.getItems(), (SyncBaseItem) target);
            } else if (selectedGroup.canCollect() && target instanceof SyncResourceItem) {
                commandService.collect(selectedGroup.getItems(), (SyncResourceItem) target);
            } else if (target instanceof SyncBoxItem) {
                commandService.pickupBox(selectedGroup.getItems(), (SyncBoxItem) target);
            }
        } else {
            this.selectedTargetSyncItem = target;
            onTargetSelectionItemChanged(this.selectedTargetSyncItem);
        }
    }

    public void setItemGroupSelected(Group selectedGroup) {
//    TODO    if (hasOwnSelection() && selectedGroup.getCount() == 1) {
//    TODO        if (selectedGroup.getFirst().hasSyncItemContainer()) {
//    TODO            if (!this.selectedGroup.equals(selectedGroup) && this.selectedGroup.canMove()) {
//    TODO                commandService.loadContainer(selectedGroup.getFirst(), this.selectedGroup.getItems());
//    TODO                clearSelection();
//    TODO                return;
//    TODO            }
//    TODO        } else if (!selectedGroup.getFirst().isReady()) {
//    TODO            commandService.finalizeBuild(this.selectedGroup.getItems(), selectedGroup.getFirst());
//    TODO            return;
//    TODO        }
//    TODO    }
        clearSelection();
        this.selectedGroup = selectedGroup;
        selectionEventEventTrigger.fire(new SelectionEvent(SelectionEvent.Type.OWN, selectedGroup));
    }

    public void selectRectangle(Rectangle2D rectangle) {
        Collection<SyncBaseItem> selectedItems = storyboardService.getMyItemsInRegion(rectangle);
        if (selectedItems.isEmpty()) {
            clearSelection();
        } else {
            setItemGroupSelected(new Group(selectedItems));
        }
    }

    public void keepOnlyOwnOfType(BaseItemType baseItemType) {
        selectedGroup.keepOnlyOwnOfType(baseItemType);
        fireOwnItemSelectionChanged(selectedGroup);
    }

    public void clearSelection() {
        selectedTargetSyncItem = null;
        selectedGroup = null;

//   TODO     for (SelectionListener listener : new ArrayList<SelectionListener>(listeners)) {
//            listener.onSelectionCleared();
//        }
//   TODO      CursorHandler.getInstance().onSelectionCleared();
    }

    private void onTargetSelectionItemChanged(SyncItem selection) {
//  TODO      for (SelectionListener listener : new ArrayList<SelectionListener>(listeners)) {
//            listener.onTargetSelectionChanged(selection);
//        }
// TODO       CursorHandler.getInstance().onSelectionCleared();
    }

    @Deprecated
    private void fireOwnItemSelectionChanged(Group selection) {
//  TODO      for (SelectionListener listener : new ArrayList<SelectionListener>(listeners)) {
//            listener.onOwnSelectionChanged(selection);
//        }
    }

    public void itemKilled(SyncItem syncItem) {
        if (syncItem.equals(selectedTargetSyncItem)) {
            clearSelection();
        }

        if (selectedGroup != null && syncItem instanceof SyncBaseItem && selectedGroup.contains((SyncBaseItem) syncItem)) {
            selectedGroup.remove((SyncBaseItem) syncItem);
            if (selectedGroup.isEmpty()) {
                clearSelection();
            } else {
                fireOwnItemSelectionChanged(selectedGroup);
            }
        }
    }

    public void refresh() {
        if (selectedGroup != null) {
            setItemGroupSelected(selectedGroup);
        } else if (selectedTargetSyncItem != null) {
            onTargetSelectionItemChanged(selectedTargetSyncItem);
        }
    }

    public SyncItem getSelectedTargetSyncItem() {
        return selectedTargetSyncItem;
    }
}