package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
public class SendFactorizeCommandTipTask extends AbstractTipTask {
    private int itemTypeToFactorized;

    public void init(int itemTypeToFactorized) {
        this.itemTypeToFactorized = itemTypeToFactorized;
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
    protected void onCommandSent(BaseCommand baseCommand) {
        if (baseCommand instanceof FactoryCommand && ((FactoryCommand) baseCommand).getToBeBuiltId() == itemTypeToFactorized) {
            onSucceed();
        }
    }
}
