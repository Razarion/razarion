package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.tip.visualization.GuiTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.12.2016.
 */
@Dependent
public class OpenInventoryTipTask extends AbstractTipTask {
    private Logger logger = Logger.getLogger(OpenInventoryTipTask.class.getName());
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
    public GuiTipVisualization createGuiTipVisualization() {
        if (getGameTipVisualConfig().getLeftArrowLeftMouseGuiImageId() != null) {
            return new GuiTipVisualization(this::providePosition, getGameTipVisualConfig().getLeftArrowLeftMouseGuiImageId());
        } else {
            logger.warning("No image defined for GameTipVisualConfig.leftArrowLeftMouseGuiImageId");
            return null;
        }
    }

    private Index providePosition() {
        Rectangle rectangle = cockpitService.getInventoryButtonLocation();
        return new Index(rectangle.endX(), (rectangle.startY() + rectangle.endY()) / 2);
    }
}
