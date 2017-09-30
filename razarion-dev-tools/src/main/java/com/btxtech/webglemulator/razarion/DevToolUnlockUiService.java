package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.uiservice.unlock.UnlockUiService;

import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 29.09.2017.
 */
@Singleton
public class DevToolUnlockUiService extends UnlockUiService {
    @Override
    protected void unlockViaCrystalCall(LevelUnlockConfig levelUnlockConfig, Consumer<UnlockResultInfo> callback) {
        throw new UnsupportedOperationException();
    }
}
