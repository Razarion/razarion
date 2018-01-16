package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.control.Scene;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.12.2016.
 */
@Dependent
public class ScrollTipTask extends AbstractTipTask implements ViewService.ViewFieldListener {
    private Logger logger = Logger.getLogger(ScrollTipTask.class.getName());
    private static final long SCROLL_DELAY = 10000;
    private static final long TIMER_DELAY = 500;
    @Inject
    private ViewService viewService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private GameUiControl gameUiControl;
    private SimpleScheduledFuture simpleScheduledFuture;
    private DecimalPosition terrainPositionHint;
    private long dialogShowTimestamp;
    // private SplashTipVisualization splashTipVisualization;
    private InGameDirectionVisualization inGameDirectionVisualization;
    private boolean dialogVisible;
    private Runnable dialogCloseCallback;
    private boolean ended;
    private Integer mapImageId;

    public void init(DecimalPosition terrainPositionHint, Integer mapImageId) {
        this.mapImageId = mapImageId;
        selectionHandler.clearSelection(true);
        this.terrainPositionHint = terrainPositionHint;
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    protected void internalStart() {
        viewService.addViewFieldListeners(this);
        dialogShowTimestamp = System.currentTimeMillis();
        if (mapImageId != null) {
            startTimer();
        }
    }

    @Override
    protected void internalCleanup() {
        ended = true;
        viewService.removeViewFieldListeners(this);
        stopTimer();
    }

    @Override
    public void onOwnSelectionChanged(Group selectedGroup) {
        showScrollDialog(true);
    }

    @Override
    protected void onOtherSelectionChanged(SyncItemSimpleDto selectedOther) {
        showScrollDialog(true);
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        dialogShowTimestamp = System.currentTimeMillis();
        showScrollDialog(false);
        startTimer();
    }

    private void onTimer() {
        if (dialogShowTimestamp + SCROLL_DELAY < System.currentTimeMillis()) {
            stopTimer();
            showScrollDialog(true);
        }
    }

    @Override
    public InGameDirectionVisualization createInGameDirectionVisualization() {
        inGameDirectionVisualization = new InGameDirectionVisualization(getGameTipVisualConfig().getDirectionShape3DId(), terrainPositionHint, !dialogVisible, getNativeMatrixFactory());
        return inGameDirectionVisualization;
    }

    private void startTimer() {
        if (ended) {
            return;
        }
        if (simpleScheduledFuture == null) {
            simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(TIMER_DELAY, true, this::onTimer, SimpleExecutorService.Type.TIP_SCROLL);
        }
    }

    private void stopTimer() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
        }
    }

    private void showScrollDialog(boolean splashVisible) {
        if (mapImageId == null) {
            inGameDirectionVisualization.setVisible(!splashVisible);
            return;
        }
        if (this.dialogVisible == splashVisible) {
            return;
        }
        this.dialogVisible = splashVisible;
        if (splashVisible) {
            if (!ended) {
                modalDialogManager.showScrollTipDialog(setupScrollTipDialogModel());
            }
            stopTimer();
        } else {
            if (dialogCloseCallback != null) {
                dialogCloseCallback.run();
            }
        }
        inGameDirectionVisualization.setVisible(!splashVisible);
    }

    private ScrollTipDialogModel setupScrollTipDialogModel() {
        ScrollTipDialogModel scrollTipDialogModel = new ScrollTipDialogModel();
        scrollTipDialogModel.setScrollDialogKeyboardImageId(getGameTipVisualConfig().getScrollDialogKeyboardImageId());
        scrollTipDialogModel.setScrollDialogMapImageId(mapImageId);
        Scene scene = gameUiControl.getCurrentScene();
        if (scene != null) {
            ScrollUiQuest scrollUiQuest = scene.getSceneConfig().getScrollUiQuest();
            if (scrollUiQuest != null) {
                scrollTipDialogModel.setDialogTitle(scrollUiQuest.getTitle());
                scrollTipDialogModel.setDialogMessage(scrollUiQuest.getDescription());
            } else {
                logger.warning("ScrollTipTask.getDialogMessage() scrollUiQuest == null");
                scrollTipDialogModel.setDialogTitle("");
                scrollTipDialogModel.setDialogMessage("");
            }
        } else {
            logger.warning("ScrollTipTask.getDialogMessage() scene == null");
            scrollTipDialogModel.setDialogTitle("");
            scrollTipDialogModel.setDialogMessage("");
        }
        scrollTipDialogModel.setDialogOpenCallback(this::onDialogOpened);
        scrollTipDialogModel.setDialogCloseCallback(this::onDialogClosed);
        return scrollTipDialogModel;
    }

    public void onDialogClosed() {
        dialogCloseCallback = null;
        if (ended) {
            return;
        }
        selectionHandler.clearSelection(true);
        startTimer();
        showScrollDialog(false);
        dialogShowTimestamp = System.currentTimeMillis();
    }

    public void onDialogOpened(Runnable openDialog) {
        this.dialogCloseCallback = openDialog;
    }
}
