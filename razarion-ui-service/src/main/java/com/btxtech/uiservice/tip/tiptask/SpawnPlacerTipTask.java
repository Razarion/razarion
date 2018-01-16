package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.control.Scene;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerListener;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.tip.visualization.InGamePositionTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
@Dependent
public class SpawnPlacerTipTask extends AbstractTipTask implements BaseItemPlacerListener, ViewService.ViewFieldListener {
    private static final long TIMER_DELAY = 10000;
    private Logger logger = Logger.getLogger(SpawnPlacerTipTask.class.getName());
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ViewService viewService;
    private SimpleScheduledFuture simpleScheduledFuture;
    private int spawnItemTypeId;
    private DecimalPosition positionHint;
    private Integer dialogMapImageId;

    public void init(int spawnItemTypeId, DecimalPosition positionHint, Integer dialogMapImageId) {
        this.spawnItemTypeId = spawnItemTypeId;
        this.positionHint = positionHint;
        this.dialogMapImageId = dialogMapImageId;
    }

    @Override
    public void internalStart() {
        baseItemPlacerService.addListener(this);
        if (dialogMapImageId != null) {
            viewService.addViewFieldListeners(this);
            startTimer();
        }
    }

    @Override
    public void internalCleanup() {
        stopTimer();
        if (dialogMapImageId != null) {
            viewService.removeViewFieldListeners(this);
        }
        baseItemPlacerService.removeListener(this);
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        InGamePositionTipVisualization visualization = new InGamePositionTipVisualization(getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getBaseItemPlacerCornerColor(), getGameTipVisualConfig().getBaseItemPlacerShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId(), getNativeMatrixFactory());
        terrainUiService.getTerrainPosition(positionHint, visualization::setPosition);
        return visualization;
    }

    @Override
    protected void onSpawnSyncItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.itemTypeId == spawnItemTypeId) {
            onSucceed();
        }
    }

    @Override
    public void activatePlacer(BaseItemPlacer baseItemPlacer) {
        // Ignore
    }

    @Override
    public void deactivatePlacer(boolean canceled) {
        if (canceled) {
            onFailed();
        }
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        startTimer();
    }

    public void startTimer() {
        stopTimer();
        simpleScheduledFuture = simpleExecutorService.schedule(TIMER_DELAY, this::onTimer, SimpleExecutorService.Type.TIP_SPAWN);
    }

    public void stopTimer() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
        }
    }

    private void onTimer() {
        modalDialogManager.showScrollTipDialog(setupScrollTipDialogModel());
    }

    private ScrollTipDialogModel setupScrollTipDialogModel() {
        ScrollTipDialogModel scrollTipDialogModel = new ScrollTipDialogModel();
        scrollTipDialogModel.setScrollDialogKeyboardImageId(getGameTipVisualConfig().getScrollDialogKeyboardImageId());
        scrollTipDialogModel.setScrollDialogMapImageId(dialogMapImageId);
        Scene scene = gameUiControl.getCurrentScene();
        if (scene != null) {
            QuestConfig questConfig = scene.getSceneConfig().getQuestConfig();
            if (questConfig != null) {
                scrollTipDialogModel.setDialogTitle(questConfig.getTitle());
                scrollTipDialogModel.setDialogMessage(questConfig.getDescription());
            } else {
                logger.warning("ScrollTipTask.getDialogMessage() questConfig == null");
                scrollTipDialogModel.setDialogTitle("");
                scrollTipDialogModel.setDialogMessage("");
            }
        } else {
            logger.warning("ScrollTipTask.getDialogMessage() scene == null");
            scrollTipDialogModel.setDialogTitle("");
            scrollTipDialogModel.setDialogMessage("");
        }
        scrollTipDialogModel.setDialogCloseCallback(this::startTimer);
        return scrollTipDialogModel;
    }
}
