package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;

import java.util.logging.Logger;

public class TestServerSystemConnection extends AbstractServerSystemConnection {
    private Logger logger = Logger.getLogger(TestServerSystemConnection.class.getName());

    @Override
    protected void sendToServer(String text) {
        logger.fine("sendToServer(): " + text);
    }

    @Override
    protected void onLifecyclePacket(LifecyclePacket lifecyclePacket) {
        logger.fine("onLifecyclePacket(): " + lifecyclePacket);
    }

    @Override
    protected String toJson(Object param) {
        return null;
    }

    @Override
    protected Object fromJson(String jsonString, SystemConnectionPacket packet) {
        return null;
    }

    @Override
    public void init() {
        logger.fine("init()");
    }

    @Override
    public void close() {
        logger.fine("close()");
    }
}
