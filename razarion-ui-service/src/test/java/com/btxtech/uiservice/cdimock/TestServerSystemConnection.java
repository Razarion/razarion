package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.control.AbstractServerSystemConnection;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;

import java.util.logging.Logger;

public class TestServerSystemConnection extends AbstractServerSystemConnection {
    private Logger logger = Logger.getLogger(TestServerSystemConnection.class.getName());

    public TestServerSystemConnection(Boot boot, ChatUiService chatUiService, InventoryUiService inventoryUiService, UserUiService userUiService, GameUiControl gameUiControl) {
        super(boot, chatUiService, inventoryUiService, userUiService, gameUiControl);
    }

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
