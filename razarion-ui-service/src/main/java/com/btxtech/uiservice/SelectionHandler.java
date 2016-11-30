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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: May 19, 2009
 * Time: 9:09:40 PM
 */
@ApplicationScoped
public class SelectionHandler {
    private Logger logger = Logger.getLogger(SelectionHandler.class.getName());
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

    public Collection<SyncBaseItem> getAllowed2FinalizeBuild(SyncBaseItem ableToBuild) {
        if (selectedGroup == null) {
            throw new IllegalStateException("selectedGroup == null");
        }
        Collection<SyncBaseItem> builders = selectedGroup.getBuilders(ableToBuild.getBaseItemType());
        if (builders.isEmpty()) {
            throw new IllegalStateException("builders.isEmpty()");
        }
        return builders;
    }

    public void setTargetSelected(SyncItem target) {
        if (selectedGroup != null) {
            if (selectedGroup.canAttack() && target instanceof SyncBaseItem) {
                commandService.attack(selectedGroup.getItems(), (SyncBaseItem) target);
            } else if (selectedGroup.canCollect() && target instanceof SyncResourceItem) {
                commandService.harvest(selectedGroup.getItems(), (SyncResourceItem) target);
            } else if (selectedGroup.canMove() && target instanceof SyncBoxItem) {
                commandService.pickupBox(selectedGroup.getMovables(), (SyncBoxItem) target);
            }
        } else {
            // TODO this may be wrong
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
//    TODO        } else if (!selectedGroup.getFirst().isBuildup()) {
//    TODO            commandService.finalizeBuild(this.selectedGroup.getItems(), selectedGroup.getFirst());
//    TODO            return;
//    TODO        }
//    TODO    }
        clearSelection();
        this.selectedGroup = selectedGroup;
        selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup));
    }

    public void selectRectangle(Rectangle2D rectangle) {
        Collection<SyncBaseItem> selectedItems = syncItemContainerService.findBaseItemInRect(rectangle);
        if (selectedItems.isEmpty()) {
            clearSelection();
        } else {
            SyncBaseItem enemy = null;
            Collection<SyncBaseItem> own = new ArrayList<>();
            for (SyncBaseItem selectedItem : selectedItems) {
                if (storyboardService.isMyOwnProperty(selectedItem)) {
                    own.add(selectedItem);
                } else {
                    enemy = selectedItem;
                }
            }

            if (!own.isEmpty()) {
                setItemGroupSelected(new Group(selectedItems));
            } else if (enemy != null) {
                onTargetSelectionItemChanged(enemy);
            } else {
                logger.warning("SelectionHandler.selectRectangle() unknown state");
            }

        }
    }

    public void keepOnlyOwnOfType(BaseItemType baseItemType) {
        selectedGroup.keepOnlyOwnOfType(baseItemType);
        fireOwnItemSelectionChanged(selectedGroup);
    }

    public void clearSelection() {
        selectedTargetSyncItem = null;
        selectedGroup = null;
        selectionEventEventTrigger.fire(new SelectionEvent());
    }

    private void onTargetSelectionItemChanged(SyncItem target) {
        selectionEventEventTrigger.fire(new SelectionEvent(target));
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