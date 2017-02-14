package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;

public abstract class CursorService {
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ItemTypeService itemTypeService;
    private CursorType currentCursorType;
    private boolean currentAllowed;
    private boolean currentPointer;
    private boolean currentDefault;

    protected abstract void setCursorInternal(CursorType cursorType, boolean allowed);

    protected abstract void setDefaultCursorInternal();

    protected abstract void setPointerCursorInternal();

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectionEvent.getType() == SelectionEvent.Type.CLEAR) {
            setDefaultCursor();
        }
    }

    // Need to be public due to the weld proxies for the dev-tools
    public void handleMouseOverBaseItem(SyncBaseItemSimpleDto syncBaseItem, DecimalPosition terrainPosition) {
        if (!selectionHandler.hasOwnSelection() || !isNotMyself(syncBaseItem)) {
            setPointerCursor();
            return;
        }

        if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (cockpitMode.getMode() == CockpitMode.Mode.UNLOAD && baseItemType.getItemContainerType() != null) {
                // TODO
                setPointerCursor();
//                SyncItemContainer syncItemContainer = syncBaseItem.getSyncItemContainer();
//                boolean allowed = syncItemContainer.atLeastOneAllowedToLoad(selectionHandler.getOwnSelection().getItems());
//                setCursor(CursorType.LOAD, allowed);
            } else {
                if (!syncBaseItem.checkBuildup()) {
                    Collection<SyncBaseItemSimpleDto> builder = selectionHandler.getOwnSelection().getBuilders(syncBaseItem.getItemTypeId());
                    if (!builder.isEmpty()) {
                        setCursor(CursorType.FINALIZE_BUILD, true);
                    } else {
                        setPointerCursor();
                    }
                } else {
                    setPointerCursor();
                }
            }
        } else if (baseItemUiService.isMyEnemy(syncBaseItem)) {
            Collection<SyncBaseItemSimpleDto> attackers = selectionHandler.getOwnSelection().getAttackers(syncBaseItem);
            if (!attackers.isEmpty()) {
                setCursor(CursorType.ATTACK, true);
            } else {
                setPointerCursor();
            }
        } else {
            setPointerCursor();
        }
    }

    public void handleMouseOverResourceItem() {
        if (!selectionHandler.hasOwnSelection()) {
            setPointerCursor();
            return;
        }
        Collection<SyncBaseItemSimpleDto> harvesters = selectionHandler.getOwnSelection().getHarvesters();
        if (!harvesters.isEmpty()) {
            setCursor(CursorType.COLLECT, true);
        } else {
            setPointerCursor();
        }
    }

    public void handleMouseOverBoxItem() {
        if (!selectionHandler.hasOwnSelection()) {
            setPointerCursor();
            return;
        }
        Collection<SyncBaseItemSimpleDto> movables = selectionHandler.getOwnSelection().getMovables();
        if (!movables.isEmpty()) {
            setCursor(CursorType.PICKUP, true);
        } else {
            setPointerCursor();
        }
    }

    // Need to be public due to the weld proxies for the dev-tools
    public void handleMouseOverTerrain(DecimalPosition terrainPosition) {
        if (!selectionHandler.hasOwnSelection()) {
            setDefaultCursor();
            return;
        }
        if (cockpitMode.getMode() == CockpitMode.Mode.UNLOAD) {
            setCursor(CursorType.UNLOAD, atLeastOnAllowedForUnload(terrainPosition));
        } else {
            Collection<SyncBaseItemSimpleDto> movables = selectionHandler.getOwnSelection().getMovables();
            if (!movables.isEmpty()) {
                setCursor(CursorType.GO, !terrainUiService.overlap(terrainPosition));
            } else {
                setPointerCursor();
            }
        }
    }

    public void handleItemPlaceActivated() {
        setDefaultCursor();
    }

    private boolean atLeastOnAllowedForUnload(DecimalPosition position) {
        // TODO
//        for (SyncBaseItemSimpleDto syncBaseItem : selectionHandler.getOwnSelection().getItems()) {
//            if (syncBaseItem.getSyncItemContainer() != null) {
//                SyncItemContainer syncItemContainer = syncBaseItem.getSyncItemContainer();
//                if (syncItemContainer.atLeastOneAllowedToUnload(position)) {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    private boolean isNotMyself(SyncBaseItemSimpleDto me) {
        for (SyncBaseItemSimpleDto syncBaseItem : selectionHandler.getOwnSelection().getItems()) {
            if (syncBaseItem.equals(me)) {
                return false;
            }
        }
        return true;
    }

    private void setDefaultCursor() {
        if (currentDefault) {
            return;
        }

        setDefaultCursorInternal();

        currentDefault = true;
        currentCursorType = null;
        currentPointer = false;
    }

    private void setPointerCursor() {
        if (currentPointer) {
            return;
        }

        setPointerCursorInternal();

        currentPointer = true;
        currentCursorType = null;
        currentDefault = false;
    }


    private void setCursor(CursorType cursorType, boolean allowed) {
        Objects.requireNonNull(cursorType, "CursorType must be set");

        if (currentCursorType == cursorType && currentAllowed == allowed) {
            return;
        }

        setCursorInternal(cursorType, allowed);

        currentCursorType = cursorType;
        currentAllowed = allowed;
        currentDefault = false;
        currentPointer = false;
    }
}