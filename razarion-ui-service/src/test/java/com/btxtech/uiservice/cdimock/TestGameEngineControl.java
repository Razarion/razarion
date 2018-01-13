package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.uiservice.control.GameEngineControl;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestGameEngineControl extends GameEngineControl {

    @Override
    public boolean isStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onLoaded() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object singleData) {
        throw new UnsupportedOperationException();
    }
}
