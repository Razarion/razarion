package com.btxtech.uiservice.tip;

import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.renderer.task.tip.TipRenderTask;
import com.btxtech.uiservice.tip.tiptask.AbstractTipTask;
import com.btxtech.uiservice.tip.tiptask.CommandInfo;
import com.btxtech.uiservice.tip.tiptask.TipTaskContainer;
import com.btxtech.uiservice.tip.tiptask.TipTaskFactory;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualizationService;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 21.08.12
 * Time: 22:48
 */
@ApplicationScoped
public class GameTipService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TipRenderTask tipRenderTask;
    @Inject
    private GuiTipVisualizationService guiTipVisualizationService;
    @Inject
    private TipTaskFactory tipTaskFactory;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ViewService viewService;
    private TipTaskContainer tipTaskContainer;
    private InGameTipVisualization inGameTipVisualization;
    private InGameDirectionVisualization inGameDirectionVisualization;
    private AbstractGuiTipVisualization guiTipVisualization;

    public void start(GameTipConfig gameTipConfig) {
        try {
            if (gameTipConfig == null) {
                return;
            }
            tipTaskContainer = tipTaskFactory.create(this, gameTipConfig);
            startTipTask();
        } catch (Exception e) {
            exceptionHandler.handleException("GameTipService.start()", e);
        }
    }

    public void stop() {
        try {
            if (tipTaskContainer == null) {
                return;
            }
            tipTaskContainer.cleanup();
            cleanupVisualization();
            tipTaskContainer = null;
        } catch (Exception e) {
            exceptionHandler.handleException("GameTipService.stop()", e);
        }
    }

    private void startTipTask() {
        AbstractTipTask currentTipTask = tipTaskContainer.getCurrentTask();
        if (currentTipTask.isFulfilled()) {
            tipTaskContainer.next();
            currentTipTask = tipTaskContainer.getCurrentTask();
        }
        currentTipTask.start();
        startVisualization(currentTipTask);
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (tipTaskContainer != null) {
            tipTaskContainer.onSelectionChanged(selectionEvent);
        }
    }

    public void onCommandSent(CommandInfo commandInfo) {
        if (tipTaskContainer != null) {
            tipTaskContainer.onCommandSent(commandInfo);
        }
    }

    public void onSyncBaseItemIdle(SyncBaseItemSimpleDto syncBaseItem) {
        if (tipTaskContainer != null && baseItemUiService.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onSyncBaseItemIdle(syncBaseItem);
        }
    }

    public void onSpawnSyncItem(SyncBaseItemSimpleDto syncBaseItem) {
        if (tipTaskContainer != null && baseItemUiService.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onSpawnSyncItem(syncBaseItem);
        }
    }

    public void onInventoryDialogOpened() {
        if (tipTaskContainer != null) {
            tipTaskContainer.onInventoryDialogOpened();
        }
    }

    public void onInventoryDialogClosed() {
        if (tipTaskContainer != null) {
            tipTaskContainer.onInventoryDialogClosed();
        }
    }

    public void onInventoryItemPlacerActivated(InventoryItem inventoryItem) {
        if (tipTaskContainer != null) {
            tipTaskContainer.onInventoryItemPlacerActivated(inventoryItem);
        }
    }

    public void onTaskFailed() {
        try {
            cleanupVisualization();
            tipTaskContainer.backtrackTask();
            startTipTask();
        } catch (Exception e) {
            exceptionHandler.handleException("GameTipService.onTaskFailed()", e);
        }
    }

    public void onSucceed() {
        try {
            cleanupVisualization();
            tipTaskContainer.next();
            if (!tipTaskContainer.hasTip()) {
                if (tipTaskContainer.isLastTipIdle()) {
                    tipTaskContainer.resetIndex();
                } else {
                    tipTaskContainer = null;
                    return;
                }
            }
            startTipTask();
        } catch (Exception e) {
            exceptionHandler.handleException("GameTipService.onSucceed()", e);
        }
    }

    private void startVisualization(AbstractTipTask currentTipTask) {
        inGameTipVisualization = currentTipTask.createInGameTipVisualization();
        if (inGameTipVisualization != null) {
            viewService.addViewFieldListeners(inGameTipVisualization);
            inGameTipVisualization.onViewChanged(viewService.getCurrentViewField(), viewService.getCurrentAabb());
            tipRenderTask.activate(inGameTipVisualization);
        }
        inGameDirectionVisualization = currentTipTask.createInGameDirectionVisualization();
        if (inGameDirectionVisualization != null) {
            viewService.addViewFieldListeners(inGameDirectionVisualization);
            inGameDirectionVisualization.onViewChanged(viewService.getCurrentViewField(), viewService.getCurrentAabb());
            tipRenderTask.activate(inGameDirectionVisualization);
        }
        guiTipVisualization = currentTipTask.createGuiTipVisualization();
        if (guiTipVisualization != null) {
            guiTipVisualization.start(simpleExecutorService);
            guiTipVisualizationService.activate(guiTipVisualization);
        }
    }

    private void cleanupVisualization() {
        if (inGameTipVisualization != null) {
            tipRenderTask.deactivate();
            viewService.removeViewFieldListeners(inGameTipVisualization);
            inGameTipVisualization.cleanup();
            inGameTipVisualization = null;
        }
        if (inGameDirectionVisualization != null) {
            tipRenderTask.deactivate();
            viewService.removeViewFieldListeners(inGameDirectionVisualization);
            inGameDirectionVisualization = null;
        }
        if (guiTipVisualization != null) {
            guiTipVisualization.stop();
            guiTipVisualizationService.deactivate();
            guiTipVisualization = null;
        }

    }
}
