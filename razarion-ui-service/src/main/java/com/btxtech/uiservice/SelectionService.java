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
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: May 19, 2009
 * Time: 9:09:40 PM
 */
@JsType
@Singleton
public class SelectionService {
    private final Logger logger = Logger.getLogger(SelectionService.class.getName());
    private final SelectionEventService selectionEventService;
    private final Provider<Group> groupInstance;
    private final Provider<BaseItemUiService> baseItemUiService;
    private final Provider<ResourceUiService> resourceUiService;
    private final Provider<BoxUiService> boxUiService;
    private ActionServiceListener actionServiceListener;
    private Group selectedGroup;
    private SyncItemSimpleDto selectedOtherSyncItem;

    @Inject
    public SelectionService(Provider<BoxUiService> boxUiService,
                            Provider<ResourceUiService> resourceUiService,
                            Provider<BaseItemUiService> baseItemUiService,
                            Provider<com.btxtech.uiservice.Group> groupInstance,
                            SelectionEventService selectionEventService) {
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.groupInstance = groupInstance;
        this.selectionEventService = selectionEventService;
        selectionEventService.receiveSelectionEvent(this::onOwnSelectionChanged);
    }

    public Group getOwnSelection() {
        return selectedGroup;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setActionServiceListener(ActionServiceListener actionServiceListener) {
        this.actionServiceListener = actionServiceListener;
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
        selectionEventService.fire(new SelectionEvent(syncItemSimpleDto));
    }

    private void setItemGroupSelected(Group selectedGroup) {
        clearSelection(true);
        this.selectedGroup = selectedGroup;
        selectionEventService.fire(new SelectionEvent(selectedGroup, false));
    }

    @SuppressWarnings("unused") // Called by Angular
    public void selectRectangle(double xStart, double yStart, double width, double height) {
        try {
            Rectangle2D rectangle = new Rectangle2D(xStart, yStart, width, height);
            Collection<SyncBaseItemSimpleDto> selectedBaseItems = baseItemUiService.get().findItemsInRect(rectangle);
            if (!selectedBaseItems.isEmpty()) {
                onBaseItemsSelected(selectedBaseItems);
                return;
            }
            Collection<SyncBoxItemSimpleDto> selectedBoxItems = boxUiService.get().findItemsInRect(rectangle);
            if (!selectedBoxItems.isEmpty()) {
                setOtherItemSelected(CollectionUtils.getFirst(selectedBoxItems));
                return;
            }

            Collection<SyncResourceItemSimpleDto> selectedResourceItems = resourceUiService.get().findItemsInRect(rectangle);
            if (!selectedResourceItems.isEmpty()) {
                setOtherItemSelected(CollectionUtils.getFirst(selectedResourceItems));
                return;
            }

            clearSelection(false);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "selectRectangle failed", t);
        }
    }

    public void onBaseItemsSelected(Collection<SyncBaseItemSimpleDto> selectedBaseItems) {
        Collection<SyncBaseItemSimpleDto> own = new ArrayList<>();
        SyncBaseItemSimpleDto other = null;
        for (SyncBaseItemSimpleDto selectedSyncBaseItem : selectedBaseItems) {
            if (baseItemUiService.get().isMyOwnProperty(selectedSyncBaseItem)) {
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
        selectionEventService.fire(new SelectionEvent(selectedGroup, false));
    }

    public void clearSelection(boolean suppressAudio) {
        selectedOtherSyncItem = null;
        if (selectedGroup != null) {
            selectedGroup.release();
        }
        selectedGroup = null;
        selectionEventService.fire(new SelectionEvent(suppressAudio));
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
                    selectionEventService.fire(new SelectionEvent(selectedGroup, true));
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

    public void onOwnSelectionChanged(SelectionEvent selectionEvent) {
        if (this.actionServiceListener != null) {
            this.actionServiceListener.onSelectionChanged();
        }
    }
}