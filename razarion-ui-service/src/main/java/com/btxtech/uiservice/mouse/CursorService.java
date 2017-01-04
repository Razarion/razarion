package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Objects;

public abstract class CursorService {
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private TerrainUiService terrainUiService;
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

    public void noCursor() { // TODO not used
        setDefaultCursor();
    }

    public void handleMouseMove(SyncItem syncItem, DecimalPosition terrainPosition) {
        if (syncItem != null) {
            handleItemCursor(syncItem);
        } else {
            handleTerrainCursor(terrainPosition);
        }
    }

    private void handleTerrainCursor(DecimalPosition terrainPosition) {
        if (cockpitMode.getMode() == CockpitMode.Mode.UNLOAD) {
            setCursor(CursorType.UNLOAD, atLeastOnAllowedForUnload(terrainPosition));
        } else if (cockpitMode.isMovePossible()) {
            // If water is implemented, check if selected items can move to it
            setCursor(CursorType.GO, !terrainUiService.overlap(terrainPosition));
        } else {
            setDefaultCursor();
        }
    }

    private void handleItemCursor(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (gameUiControl.isMyOwnProperty(syncBaseItem)) {
                if (cockpitMode.isLoadPossible() && syncBaseItem.getSyncItemContainer() != null && isNotMyself(syncBaseItem)) {
                    SyncItemContainer syncItemContainer = syncBaseItem.getSyncItemContainer();
                    boolean allowed = syncItemContainer.atLeastOneAllowedToLoad(selectionHandler.getOwnSelection().getItems());
                    setCursor(CursorType.LOAD, allowed);
                } else if (cockpitMode.isFinalizeBuildPossible() && !syncBaseItem.isBuildup() && isNotMyself(syncBaseItem)) {
                    setCursor(CursorType.FINALIZE_BUILD, selectionHandler.atLeastOneItemTypeAllowed2FinalizeBuild(syncBaseItem));
                } else {
                    setPointerCursor();
                }
            } else if (gameUiControl.isEnemy(syncBaseItem)) {
                if (cockpitMode.isAttackPossible()) {
                    setCursor(CursorType.ATTACK, selectionHandler.atLeastOneItemTypeAllowed2Attack4Selection(syncBaseItem));
                } else {
                    setPointerCursor();
                }
            } else {
                setPointerCursor();
            }
        } else if (cockpitMode.isCollectPossible() && syncItem instanceof SyncResourceItem) {
            setCursor(CursorType.COLLECT, true);
        } else if (cockpitMode.isMovePossible() && syncItem instanceof SyncBoxItem) {
            setCursor(CursorType.PICKUP, true);
        } else {
            setPointerCursor();
        }
    }

    private boolean atLeastOnAllowedForUnload(DecimalPosition position) {
        for (SyncBaseItem syncBaseItem : selectionHandler.getOwnSelection().getItems()) {
            if (syncBaseItem.getSyncItemContainer() != null) {
                SyncItemContainer syncItemContainer = syncBaseItem.getSyncItemContainer();
                if (syncItemContainer.atLeastOneAllowedToUnload(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNotMyself(SyncBaseItem me) {
        for (SyncBaseItem syncBaseItem : selectionHandler.getOwnSelection().getItems()) {
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