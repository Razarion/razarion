package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.tip.visualization.InGamePositionTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class SendMoveCommandTipTask extends AbstractTipTask {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ModalDialogManager modalDialogManager;
    private DecimalPosition position;

    public void init(DecimalPosition position) {
        this.position = position;
        activateFailOnSelectionCleared();
    }

    @Override
    public void internalStart() {
    }

    @Override
    public boolean isFulfilled() {
        return false;
    }

    @Override
    public void internalCleanup() {
    }

    @Override
    protected void onCommandSent(CommandInfo commandInfo) {
        if (commandInfo.getType() == CommandInfo.Type.MOVE) {
            onSucceed();
        }
    }

    @Override
    public InGameTipVisualization createInGameTipVisualization() {
        InGamePositionTipVisualization visualization = new InGamePositionTipVisualization(getGameTipVisualConfig().getCornerMoveDistance(), getGameTipVisualConfig().getCornerMoveDuration(), getGameTipVisualConfig().getCornerLength(), getGameTipVisualConfig().getMoveCommandCornerColor(), getGameTipVisualConfig().getDefaultCommandShape3DId(), getGameTipVisualConfig().getOutOfViewShape3DId(), getNativeMatrixFactory());
        terrainUiService.getTerrainPosition(position, visualization::setPosition);
        return visualization;
    }
}
