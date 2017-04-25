package com.btxtech.uiservice.control;

import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;

/**
 * Created by Beat
 * 25.04.2017.
 */
public abstract class AbstractServerSystemConnection {
    protected abstract void sendToServer(String text);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, SystemConnectionPacket packet);

    public abstract void init();

    public abstract void close();

    public void onLevelChanged(LevelConfig levelConfig) {
        sendToServer(ConnectionMarshaller.marshall(SystemConnectionPacket.LEVEL_UPDATE, toJson(levelConfig.getLevelId())));
    }

    public void handleMessage(String text) {
        SystemConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, SystemConnectionPacket.class);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, packet);
        switch (packet) {
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }

}
