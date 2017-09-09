package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.GuiPointingTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.12.2016.
 */
@Dependent
public class ScrollHomeButtonTipTask extends AbstractTipTask {
    private Logger logger = Logger.getLogger(ScrollHomeButtonTipTask.class.getName());
    @Inject
    private CockpitService cockpitService;

    @Override
    protected void internalStart() {

    }

    @Override
    protected void internalCleanup() {

    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    protected void onInventoryDialogOpened() {
        onSucceed();
    }

    @Override
    public AbstractGuiTipVisualization createGuiTipVisualization() {
        if (getGameTipVisualConfig().getWestLeftMouseGuiImageId() != null) {
            return new GuiPointingTipVisualization(this::providePosition, GuiPointingTipVisualization.Direction.WEST, getGameTipVisualConfig().getWestLeftMouseGuiImageId());
        } else {
            logger.warning("No image defined for GameTipVisualConfig.leftArrowLeftMouseGuiImageId");
            return null;
        }
    }

    private Index providePosition() {
        Rectangle rectangle = cockpitService.getScrollHomeButtonLocation();
        return new Index(rectangle.endX(), (rectangle.startY() + rectangle.endY()) / 2);
    }
}
