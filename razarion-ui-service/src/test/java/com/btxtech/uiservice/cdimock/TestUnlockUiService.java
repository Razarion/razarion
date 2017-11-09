package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.uiservice.unlock.UnlockUiService;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 09.11.2017.
 */
@ApplicationScoped
public class TestUnlockUiService extends UnlockUiService {
    @Override
    protected void unlockViaCrystalCall(LevelUnlockConfig levelUnlockConfig, Consumer<UnlockResultInfo> callback) {
        throw new UnsupportedOperationException();
    }
}
