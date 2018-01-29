package com.btxtech.client.dialog.unlock;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.CommonUrl;

/**
 * Created by Beat
 * on 14.09.2017.
 */
public class UnlockItemModel {
    private LevelUnlockConfig levelUnlockConfig;
    private UnlockDialog unlockDialog;

    public UnlockItemModel(LevelUnlockConfig levelUnlockConfig, UnlockDialog unlockDialog) {
        this.levelUnlockConfig = levelUnlockConfig;
        this.unlockDialog = unlockDialog;
    }

    public LevelUnlockConfig getLevelUnlockConfig() {
        return levelUnlockConfig;
    }

    public String getImageUrl() {
        return CommonUrl.getImageServiceUrlSafe(levelUnlockConfig.getThumbnail());
    }

    public void closeDialog() {
        unlockDialog.close();
    }
}
