package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerConnection {
    @Inject
    private GameEngineWorker gameEngineWorker;

    protected abstract void sendToServer(String text);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, ConnectionMarshaller.Package aPackage);

    public abstract void init();

    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToServer(ConnectionMarshaller.marshall(ConnectionMarshaller.Package.CREATE_BASE, toJson(position)));
    }

    public void handleMessage(String text) {
        ConnectionMarshaller.Package aPackage = ConnectionMarshaller.deMarshallPackage(text);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, aPackage);
        switch (aPackage) {

            case BASE_CREATED:
                gameEngineWorker.onServerBaseCreated((PlayerBaseInfo)param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + aPackage);
        }
    }

}
