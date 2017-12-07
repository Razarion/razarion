package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SyncBaseItemMonitor;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

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

    public void clear() {
        currentCursorType = null;
        currentAllowed = false;
        currentPointer = false;
        currentDefault = false;
    }

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectionEvent.getType() == SelectionEvent.Type.CLEAR) {
            setDefaultCursor();
        }
    }

    // Need to be public due to the weld proxies for the dev-tools
    public void handleMouseOverBaseItem(SyncBaseItemSimpleDto syncBaseItem) {
        if (!selectionHandler.hasOwnSelection() || !isNotMyself(syncBaseItem)) {
            setPointerCursor();
            return;
        }

        if (baseItemUiService.isMyOwnProperty(syncBaseItem)) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (syncBaseItem.checkBuildup() && baseItemType.getItemContainerType() != null && selectionHandler.hasOwnSelection()) {
                setCursor(CursorType.LOAD, atLeastOneAllowedToLoad(baseItemType.getItemContainerType(), selectionHandler.getOwnSelection().getItems()));
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
            Set<TerrainType> terrainTypes = selectionHandler.getOwnSelection().getMovableTerrainTypes();
            if (!terrainTypes.isEmpty()) {
                setCursor(CursorType.GO, terrainUiService.isAtLeaseOneTerrainFreeInDisplay(terrainPosition, terrainTypes));
            } else {
                setPointerCursor();
            }
        }
    }

    public void handleItemPlaceActivated() {
        setDefaultCursor();
    }

    private boolean atLeastOneAllowedToLoad(ItemContainerType itemContainerType, Collection<SyncBaseItemSimpleDto> ownSelection) {
        for (SyncBaseItemSimpleDto syncBaseItem : ownSelection) {
            if (itemContainerType.isAbleToContain(syncBaseItem.getItemTypeId())) {
                return true;
            }
        }
        return false;
    }

    private boolean atLeastOnAllowedForUnload(DecimalPosition unloadPosition) {
        for (SyncBaseItemMonitor syncBaseItemMonitor : selectionHandler.getOwnSelection().getSyncBaseItemsMonitors()) {
            SyncBaseItemSimpleDto syncBaseItemSimpleDto = syncBaseItemMonitor.getSyncBaseItemState().getSyncBaseItem();
            if (syncBaseItemSimpleDto.getContainingItemCount() > 0) {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId());
                ItemContainerType itemContainerType = baseItemType.getItemContainerType();
                if (syncBaseItemMonitor.getPosition2d().getDistance(unloadPosition) - baseItemType.getPhysicalAreaConfig().getRadius() <= itemContainerType.getRange()) {
                    if (terrainUiService.isTerrainFreeInDisplay(unloadPosition, baseItemType.getPhysicalAreaConfig().getRadius(), SyncItemContainer.DEFAULT_UNLOAD_TERRAIN_TYPE)) {
                        return true;
                    }
                }
            }
        }
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