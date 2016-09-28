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

package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.uiservice.GroupSelectionFrame;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 16.11.2010
 * Time: 22:52:52
 */
@ApplicationScoped
public class CockpitMode  {
    public enum Mode {
        UNLOAD,
    }

//    public interface ToBeBuildPlacerListener {
//        void onToBeBuildPlacerSet(ToBeBuildPlacer toBeBuildPlacer);
//    }

//    public interface CockpitModeListener {
//        void onCockpitModChanged(Mode mode);
//    }

    private Logger logger = Logger.getLogger(CockpitMode.class.getName());
    private Mode mode;
    private boolean isMovePossible;
    private boolean isLoadPossible;
    private boolean isAttackPossible;
    private boolean isCollectPossible;
    private boolean isFinalizeBuildPossible;
    private GroupSelectionFrame groupSelectionFrame;
    // private InventoryItemPlacer inventoryItemPlacer;
    // private ToBeBuildPlacer toBeBuildPlacer;
    // private ToBeBuildPlacerListener toBeBuildPlacerListener;
    // private ToBeBuildPlacerRenderTask toBeBuildPlacerRenderTask;
//    private Collection<CockpitModeListener> cockpitModeListeners = new ArrayList<CockpitModeListener>();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            if (mode != null) {
                groupSelectionFrame = null;
                // inventoryItemPlacer = null;
            }
//            for (CockpitModeListener cockpitModeListener : cockpitModeListeners) {
//                cockpitModeListener.onCockpitModChanged(mode);
//            }
        }
    }

    public void reset() {
        setMode(null);
        clearPossibilities();
        groupSelectionFrame = null;
        // inventoryItemPlacer = null;
    }

    public void onTargetSelectionChanged(SyncItem selection) {
    }

    public void onSelectionCleared() {
        clearPossibilities();
    }

    public void onOwnSelectionChanged(Group selectedGroup) {
        setMode(null);

        if (selectedGroup.canMove()) {
            isMovePossible = true;
            isLoadPossible = true;
        } else {
            isMovePossible = false;
            isLoadPossible = false;
        }
        isAttackPossible = selectedGroup.canAttack();
        isCollectPossible = selectedGroup.canCollect();
        isFinalizeBuildPossible = selectedGroup.canFinalizeBuild();
    }

    public boolean isMovePossible() {
        return isMovePossible;
    }

    public boolean isLoadPossible() {
        return isLoadPossible;
    }

    public boolean isAttackPossible() {
        return isAttackPossible;
    }

    public boolean isCollectPossible() {
        return isCollectPossible;
    }

    public boolean isFinalizeBuildPossible() {
        return isFinalizeBuildPossible;
    }

    public void setGroupSelectionFrame(GroupSelectionFrame groupSelectionFrame) {
        this.groupSelectionFrame = groupSelectionFrame;
        // inventoryItemPlacer = null;
        // toBeBuildPlacer = null;
    }

    public boolean hasGroupSelectionFrame() {
        return groupSelectionFrame != null;
    }

    public GroupSelectionFrame getGroupSelectionFrame() {
        return groupSelectionFrame;
    }

//    public void setInventoryItemPlacer(InventoryItemPlacer inventoryItemPlacer) {
//        SelectionHandler.getInstance().clearSelection();
//        this.inventoryItemPlacer = inventoryItemPlacer;
//        groupSelectionFrame = null;
//        toBeBuildPlacer = null;
//    }
//
//    public boolean hasInventoryItemPlacer() {
//        return inventoryItemPlacer != null;
//    }
//
//    public InventoryItemPlacer getInventoryItemPlacer() {
//        return inventoryItemPlacer;
//    }
//
//    public ToBeBuildPlacer getToBeBuildPlacer() {
//        return toBeBuildPlacer;
//    }
//
//    public boolean hasToBeBuildPlacer() {
//        return toBeBuildPlacer != null;
//    }
//
//    public void setToBeBuildPlacer(ToBeBuildPlacer toBeBuildPlacer) {
//        this.toBeBuildPlacer = toBeBuildPlacer;
//        groupSelectionFrame = null;
//        inventoryItemPlacer = null;
//        if (toBeBuildPlacerListener != null) {
//            toBeBuildPlacerListener.onToBeBuildPlacerSet(toBeBuildPlacer);
//        }
//        if (toBeBuildPlacer != null) {
//            OverlayPanel.getInstance().create();
//            toBeBuildPlacerRenderTask = new ToBeBuildPlacerRenderTask(OverlayPanel.getInstance().getCanvas().getContext2d());
//            Renderer.getInstance().startOverlayRenderTask(toBeBuildPlacerRenderTask);
//        } else {
//            OverlayPanel.getInstance().destroy();
//            Renderer.getInstance().stopOverlayRenderTask(toBeBuildPlacerRenderTask);
//            toBeBuildPlacerRenderTask = null;
//        }
//    }
//
//    public void setToBeBuildPlacerListener(ToBeBuildPlacerListener toBeBuildPlacerListener) {
//        this.toBeBuildPlacerListener = toBeBuildPlacerListener;
//    }

//    public void onEscape() {
//        CockpitMode.getInstance().setToBeBuildPlacer(null);
//        if (hasInventoryItemPlacer()) {
//            CockpitMode.getInstance().setInventoryItemPlacer(null);
//        } else if (hasToBeBuildPlacer()) {
//            CockpitMode.getInstance().setToBeBuildPlacer(null);
//        } else if (mode == Mode.UNLOAD) {
//            setMode(null);
//        }
//    }

    private void clearPossibilities() {
        isMovePossible = false;
        isLoadPossible = false;
        isAttackPossible = false;
        isCollectPossible = false;
        isFinalizeBuildPossible = false;
        groupSelectionFrame = null;
    //    toBeBuildPlacer = null;
    }

}
