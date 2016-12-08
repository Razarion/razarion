package com.btxtech.uiservice.tip;

import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.renderer.task.tip.TipRenderTask;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.btxtech.uiservice.tip.tiptask.AbstractTipTask;
import com.btxtech.uiservice.tip.tiptask.TipTaskContainer;
import com.btxtech.uiservice.tip.tiptask.TipTaskFactory;
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
    public static final long TERRAIN_HINT_DURATION_MILLIS = 1000;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TipRenderTask tipRenderTask;
    @Inject
    private TipTaskFactory tipTaskFactory;
    @Inject
    private PlanetService planetService;
    @Inject
    private StoryboardService storyboardService;
    private TipTaskContainer tipTaskContainer;
    private InGameTipVisualization inGameTipVisualization;

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
        if (tipTaskContainer != null && storyboardService.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onCommandSent(baseCommand);
        }
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        if (tipTaskContainer != null && storyboardService.isMyOwnProperty(syncBaseItem)) {
            tipTaskContainer.onSyncBaseItemIdle(syncBaseItem);
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
        inGameTipVisualization = currentTipTask.createInGameTip();
        if (inGameTipVisualization != null) {
            tipRenderTask.activate(inGameTipVisualization);
        }
    }

    private void cleanupVisualization() {
        if (inGameTipVisualization != null) {
            tipRenderTask.deactivate(inGameTipVisualization);
            inGameTipVisualization = null;
        }
    }
}
