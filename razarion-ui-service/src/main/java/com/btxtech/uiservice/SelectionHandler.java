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

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: beat
 * Date: May 19, 2009
 * Time: 9:09:40 PM
 */
@JsType
@ApplicationScoped
public class SelectionHandler {
    @Inject
    private Event<SelectionEvent> selectionEventEventTrigger;
    @Inject
    private Instance<Group> groupInstance;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Group selectedGroup;
    private SyncItemSimpleDto selectedOtherSyncItem;

    public Group getOwnSelection() {
        return selectedGroup;
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean hasOwnSelection() {
        return selectedGroup != null && !selectedGroup.isEmpty();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean hasOwnMovable() {
        return selectedGroup != null && !selectedGroup.getMovables().isEmpty();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean hasAttackers() {
        return selectedGroup != null && selectedGroup.hasAttackers();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean canAttack(int targetItemTypeId) {
        return selectedGroup != null && !selectedGroup.getAttackers(targetItemTypeId).isEmpty();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean hasHarvesters() {
        return selectedGroup != null && !selectedGroup.getHarvesters().isEmpty();
    }

    public void setOtherItemSelected(SyncItemSimpleDto syncItemSimpleDto) {
        clearSelection(true);
        selectedOtherSyncItem = syncItemSimpleDto;
        selectionEventEventTrigger.fire(new SelectionEvent(syncItemSimpleDto));
    }

    private void setItemGroupSelected(Group selectedGroup) {
        clearSelection(true);
        this.selectedGroup = selectedGroup;
        selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup, false));
    }

    @SuppressWarnings("unused") // Called by Angular
    public void selectRectangle(double xStart, double yStart, double width, double height) {
        try {
            Rectangle2D rectangle = new Rectangle2D(xStart, yStart, width, height);
            Collection<SyncBaseItemSimpleDto> selectedBaseItems = baseItemUiService.findItemsInRect(rectangle);
            if (!selectedBaseItems.isEmpty()) {
                onBaseItemsSelected(selectedBaseItems);
                return;
            }
            Collection<SyncBoxItemSimpleDto> selectedBoxItems = boxUiService.findItemsInRect(rectangle);
            if (!selectedBoxItems.isEmpty()) {
                setOtherItemSelected(CollectionUtils.getFirst(selectedBoxItems));
                return;
            }

            Collection<SyncResourceItemSimpleDto> selectedResourceItems = resourceUiService.findItemsInRect(rectangle);
            if (!selectedResourceItems.isEmpty()) {
                setOtherItemSelected(CollectionUtils.getFirst(selectedResourceItems));
                return;
            }

            clearSelection(false);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void selectPosition(DecimalPosition position) {
        SyncBaseItemSimpleDto selectedBaseItem = baseItemUiService.findItemAtPosition(position);
        if (selectedBaseItem != null) {
            onBaseItemsSelected(Collections.singletonList(selectedBaseItem));
            return;
        }
        SyncBoxItemSimpleDto selectedBoxItem = boxUiService.findItemAtPosition(position);
        if (selectedBoxItem != null) {
            setOtherItemSelected(selectedBoxItem);
            return;
        }
        SyncResourceItemSimpleDto selectedResourceItem = resourceUiService.findItemAtPosition(position);
        if (selectedResourceItem != null) {
            setOtherItemSelected(selectedResourceItem);
            return;
        }

        // clearSelection(false);
    }

    public void onBaseItemsSelected(Collection<SyncBaseItemSimpleDto> selectedBaseItems) {
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
    }

    public void keepOnlyOwnOfType(BaseItemType baseItemType) {
        selectedGroup.keepOnlyOwnOfType(baseItemType);
        selectionEventEventTrigger.fire(new SelectionEvent(selectedGroup, false));
    }

    public void clearSelection(boolean suppressAudio) {
        selectedOtherSyncItem = null;
        if (selectedGroup != null) {
            selectedGroup.release();
        }
        selectedGroup = null;
        selectionEventEventTrigger.fire(new SelectionEvent(suppressAudio));
    }

    @JsIgnore
    public void baseItemRemoved(int[] removedSyncItemIds) {
        if (selectedGroup != null) {
            boolean changed = false;
            for (int syncItemId : removedSyncItemIds) {
                if (selectedGroup.remove(syncItemId)) {
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
            for (int syncBaseItemId : removedSyncItemIds) {
                if (selectedOtherSyncItem.getId() == syncBaseItemId) {
                    clearSelection(true);
                    break;
                }
            }
        }
    }

    @JsIgnore
    public void baseItemRemoved(NativeSimpleSyncBaseItemTickInfo[] removedSyncBaseItems) {
        int[] removedIds = new int[removedSyncBaseItems.length];
        // Does not work here Arrays.stream(nativeSyncBaseItemTickInfos)
        for (int i = 0; i < removedSyncBaseItems.length; i++) {
            removedIds[i] = removedSyncBaseItems[i].id;
        }
        baseItemRemoved(removedIds);
    }

    public void onMyBaseRemoved() {
        if (selectedGroup != null) {
            clearSelection(true);
        }
    }

    public void boxItemRemove(SyncBoxItemSimpleDto syncBoxItem) {
        if (syncBoxItem.equals(selectedOtherSyncItem)) {
            clearSelection(false);
        }
    }

    public void resourceItemRemove(SyncResourceItemSimpleDto syncResourceItem) {
        if (syncResourceItem.equals(selectedOtherSyncItem)) {
            clearSelection(false);
        }
    }

    public void playbackSelection(List<Integer> selectedIds) {
        if (selectedIds.isEmpty()) {
            return;
        }
        Collection<SyncBaseItemSimpleDto> ownSelection = new ArrayList<>();
        SingleHolder<SyncItemSimpleDto> other = new SingleHolder<>();

        selectedIds.forEach(itemId -> {
            SyncBaseItemSimpleDto syncBaseItemSimpleDto = baseItemUiService.getSyncBaseItemSimpleDto4IdPlayback(itemId);
            if (syncBaseItemSimpleDto != null) {
                if (baseItemUiService.isMyOwnProperty(syncBaseItemSimpleDto)) {
                    ownSelection.add(syncBaseItemSimpleDto);
                } else {
                    other.setO(syncBaseItemSimpleDto);
                }
                return;
            }
            SyncResourceItemSimpleDto syncResourceItemSimpleDto = resourceUiService.getSyncResourceItemSimpleDto4IdPlayback(itemId);
            if (syncResourceItemSimpleDto != null) {
                other.setO(syncResourceItemSimpleDto);
            }
            SyncBoxItemSimpleDto syncBoxItemSimpleDto = boxUiService.getSyncBoxItemSimpleDto4IdPlayback(itemId);
            if (syncBoxItemSimpleDto != null) {
                other.setO(syncBoxItemSimpleDto);
            }
        });

        if (!ownSelection.isEmpty()) {
            Group group = groupInstance.get();
            group.setItems(ownSelection);
            setItemGroupSelected(group);
        } else if (!other.isEmpty()) {
            setOtherItemSelected(other.getO());
        } else {
            clearSelection(true);
        }
    }
}