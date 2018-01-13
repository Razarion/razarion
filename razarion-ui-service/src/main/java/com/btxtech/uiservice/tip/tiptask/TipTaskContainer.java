package com.btxtech.uiservice.tip.tiptask;


import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.tip.GameTipService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 17.12.12
 * Time: 12:23
 */
public class TipTaskContainer {
    private List<AbstractTipTask> abstractTipTasks = new ArrayList<>();
    private List<AbstractTipTask> current;
    private GameTipService gameTipService;
    private int currentTaskIndex = 0;

    public TipTaskContainer(GameTipService gameTipService) {
        this.gameTipService = gameTipService;
        current = abstractTipTasks;
    }

    public void add(AbstractTipTask abstractTipTask) {
        abstractTipTasks.add(abstractTipTask);
        abstractTipTask.setGameTipService(gameTipService);
    }

    public AbstractTipTask getCurrentTask() {
        return current.get(currentTaskIndex);
    }

    public void next() {
        currentTaskIndex++;
        if (hasTip() && getCurrentTask().isFulfilled()) {
            next();
        }
    }

    public boolean hasTip() {
        return current != null && currentTaskIndex < current.size();
    }

    public void backtrackTask() {
        backtrackTask(currentTaskIndex - 1);
    }

    private void backtrackTask(int taskIndex) {
        if (taskIndex < 0) {
            currentTaskIndex = 0;
        } else {
            AbstractTipTask task = current.get(taskIndex);
            if (task.isFulfilled()) {
                currentTaskIndex = taskIndex + 1;
            } else {
                backtrackTask(taskIndex - 1);
            }
        }
    }

    public void cleanup() {
        if (hasTip()) {
            getCurrentTask().cleanup();
        }
    }

    public void resetIndex() {
        currentTaskIndex = 0;
    }

    public void onSelectionChanged(SelectionEvent selectionEvent) {
        if (hasTip()) {
            getCurrentTask().onSelectionChanged(selectionEvent);
        }
    }

    public void onCommandSent(CommandInfo commandInfo) {
        if (hasTip()) {
            getCurrentTask().onCommandSent(commandInfo);
        }
    }

    public void onSyncBaseItemIdle(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (hasTip()) {
            getCurrentTask().onSyncBaseItemIdle(nativeSyncBaseItemTickInfo);
        }
    }

    public void onSpawnSyncItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (hasTip()) {
            getCurrentTask().onSpawnSyncItem(nativeSyncBaseItemTickInfo);
        }
    }

    public void onInventoryDialogOpened() {
        if (hasTip()) {
            getCurrentTask().onInventoryDialogOpened();
        }
    }

    public void onInventoryDialogClosed() {
        if (hasTip()) {
            getCurrentTask().onInventoryDialogClosed();
        }
    }

    public void onInventoryItemPlacerActivated(InventoryItem inventoryItem) {
        if (hasTip()) {
            getCurrentTask().onInventoryItemPlacerActivated(inventoryItem);
        }
    }

    public boolean isLastTipIdle() {
        return abstractTipTasks.get(abstractTipTasks.size() - 1) instanceof IdleItemTipTask;
    }
}
