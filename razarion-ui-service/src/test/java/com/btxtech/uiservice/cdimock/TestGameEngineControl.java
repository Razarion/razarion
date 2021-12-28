package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.uiservice.control.GameEngineControl;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestGameEngineControl extends GameEngineControl {
    private final Logger logger = Logger.getLogger(TestGameEngineControl.class.getName());

    @Override
    public boolean isStarted() {
        logger.fine("isStarted()");
        return true;
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        logger.fine("sendToWorker()");
    }

    @Override
    protected void onLoaded() {
        logger.fine("onLoaded()");
    }

    @Override
    protected NativeTickInfo castToNativeTickInfo(Object javaScriptObject) {
        logger.fine("castToNativeTickInfo()");
        return (NativeTickInfo) javaScriptObject;
    }

    @Override
    protected NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object singleData) {
        logger.fine("castToNativeSyncBaseItemTickInfo()");
        return (NativeSyncBaseItemTickInfo) singleData;
    }

    @Override
    protected void onConnectionLost() {
        logger.fine("onConnectionLost()");
    }
}
