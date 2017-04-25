package com.btxtech.server.connection;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class ClientSystemConnectionService {
    private final Collection<ClientSystemConnection> systemGameConnections = new ArrayList<>();

    public void onOpen(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.add(clientSystemConnection);
        }
    }

    public void onClose(ClientSystemConnection clientSystemConnection) {
        synchronized (systemGameConnections) {
            systemGameConnections.remove(clientSystemConnection);
        }
    }
}
