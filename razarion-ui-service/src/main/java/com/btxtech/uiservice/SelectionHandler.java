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

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: May 19, 2009
 * Time: 9:09:40 PM
 */
@ApplicationScoped
public class SelectionHandler {
    // private Logger logger = Logger.getLogger(SelectionHandler.class.getName());
    @Inject
    private Event<SelectionEvent> selectionEventEventTrigger;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AudioService audioService;
    @Inject
    private Instance<Group> groupInstance;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    private Group selectedGroup;
    private SyncItemSimpleDto selectedOtherSyncItem;

    public Group getOwnSelection() {
        return selectedGroup;
    }

    public boolean hasOwnSelection() {
        return selectedGroup != null && !selectedGroup.isEmpty();
    }

    private void setOtherItemSelected(SyncItemSimpleDto syncBaseItem) {
        clearSelection(true);
        selectedOtherSyncItem = syncBaseItem;
        selectionEventEventTrigger.fire(new SelectionEvent(syncBaseItem));
    }

    private void setItemGroupSelected(Group selectedGroup) {
        clearSelection(true);
        this.selectedGroup = selectedGroup;
        selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup, false));
    }

    public void selectRectangle(Rectangle2D rectangle) {
        Collection<SyncBaseItemSimpleDto> selectedBaseItems = baseItemUiService.findItemsInRect(rectangle);
        if (selectedBaseItems != null) {
            Collection<SyncBaseItemSimpleDto> own = new ArrayList<>();
            SyncBaseItemSimpleDto other = null;
            for (SyncBaseItemSimpleDto selectedSyncBaseItem : selectedBaseItems) {
                if (baseItemUiService.isMyOwnProperty(selectedSyncBaseItem)) {
                    own.add(selectedSyncBaseItem);
                } else {
                    other = selectedSyncBaseItem;
                }
            }
            if (!own.isEmpty()) {
                Group group = groupInstance.get();
                group.setItems(own);
                setItemGroupSelected(group);
            } else {
                setOtherItemSelected(other);
            }
            return;
        }
        Collection<SyncBoxItemSimpleDto> selectedBoxItems = boxUiService.findItemsInRect(rectangle);
        if (selectedBoxItems != null) {
            setOtherItemSelected(CollectionUtils.getFirst(selectedBoxItems));
            return;
        }

        Collection<SyncResourceItemSimpleDto> selectedResourceItems = resourceUiService.findItemsInRect(rectangle);
        if (selectedResourceItems != null) {
            setOtherItemSelected(CollectionUtils.getFirst(selectedResourceItems));
            return;
        }

        clearSelection(false);
    }

    public void keepOnlyOwnOfType(BaseItemType baseItemType) {
        selectedGroup.keepOnlyOwnOfType(baseItemType);
        selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup, false));
    }

    public void clearSelection(boolean suppressAudio) {
        selectedOtherSyncItem = null;
        selectedGroup = null;
        selectionEventEventTrigger.fire(new SelectionEvent(suppressAudio));
    }

    public void baseItemRemoved(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        if (selectedGroup != null) {
            boolean changed = false;
            for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
                if (selectedGroup.remove(syncBaseItem)) {
                    changed = true;
                }
            }
            if (changed) {
                if (selectedGroup.isEmpty()) {
                    clearSelection(true);
                } else {
                    selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup, true));
                }
            }
        } else if (selectedOtherSyncItem != null && selectedOtherSyncItem instanceof SyncBaseItemSimpleDto) {
            for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
                if (selectedOtherSyncItem.equals(syncBaseItem)) {
                    clearSelection(true);
                    break;
                }
            }
        }
    }

    public void onMyBaseRemoved() {
        if (selectedGroup != null) {
            clearSelection(true);
        }
    }

    public void boxItemRemove(SyncBoxItemSimpleDto syncBoxItem) { // TODO call this method
        if (syncBoxItem.equals(selectedOtherSyncItem)) {
            clearSelection(false);
        }
    }

    public void resourceItemRemove(SyncResourceItemSimpleDto syncResourceItem) { // TODO call this method
        if (syncResourceItem.equals(selectedOtherSyncItem)) {
            clearSelection(false);
        }
    }
}