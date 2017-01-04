package com.btxtech.uiservice.tip;

import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.renderer.task.tip.TipRenderTask;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.tip.tiptask.AbstractTipTask;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TipRenderTask tipRenderTask;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private GuiTipVisualizationService guiTipVisualizationService;
    @Inject
    private TipTaskFactory tipTaskFactory;
    @Inject
    private PlanetService planetService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
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

    public void onCommandSent(SyncBaseItem syncBaseItem, BaseCommand baseCommand) {
        if (tipTaskContainer != null && gameUiControl.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onCommandSent(baseCommand);
        }
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        if (tipTaskContainer != null && gameUiControl.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onSyncBaseItemIdle(syncBaseItem);
        }
    }

    public void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        if (tipTaskContainer != null && gameUiControl.isMyOwnProperty(syncBaseItem)) {
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
                tipTaskContainer.activateFallback();
                if (!tipTaskContainer.hasTip()) {
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
            terrainScrollHandler.addTerrainScrollListener(inGameTipVisualization);
            inGameTipVisualization.onScroll(terrainScrollHandler.getCurrentViewField());
            tipRenderTask.activate(inGameTipVisualization);
        }
        inGameDirectionVisualization = currentTipTask.createInGameDirectionVisualization();
        if (inGameDirectionVisualization != null) {
            terrainScrollHandler.addTerrainScrollListener(inGameDirectionVisualization);
            inGameDirectionVisualization.onScroll(terrainScrollHandler.getCurrentViewField());
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
            terrainScrollHandler.removeTerrainScrollListener(inGameTipVisualization);
            inGameTipVisualization = null;
        }
        if (inGameDirectionVisualization != null) {
            tipRenderTask.deactivate();
            terrainScrollHandler.removeTerrainScrollListener(inGameDirectionVisualization);
            inGameDirectionVisualization = null;
        }
        if (guiTipVisualization != null) {
            guiTipVisualization.stop();
            guiTipVisualizationService.deactivate();
            guiTipVisualization = null;
        }

    }
}
