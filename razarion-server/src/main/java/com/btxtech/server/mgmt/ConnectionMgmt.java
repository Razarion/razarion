package com.btxtech.server.mgmt;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ClientGameConnection;
import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 05.09.2017.
 */
@Singleton
public class ConnectionMgmt {
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;

    public List<OnlineInfo> loadAllOnlines() {
        List<OnlineInfo> onlineInfos = new ArrayList<>();
        clientSystemConnectionService.iteratorClientSystemConnection((playerSession, clientSystemConnection) -> {
            OnlineInfo onlineInfo = new OnlineInfo().setSessionId(playerSession.getHttpSessionId()).setTime(clientSystemConnection.getTime()).setDuration(clientSystemConnection.getDuration());
            if (playerSession.getUserContext() != null) {
                onlineInfo.setHumanPlayerId(playerSession.getUserContext().getHumanPlayerId());
            }
            ClientGameConnection clientGameConnection = clientGameConnectionService.getClientGameConnection(playerSession);
            if (clientGameConnection != null) {
                onlineInfo.setMultiplayerDate(clientGameConnection.getTime()).setMultiplayerDuration(clientGameConnection.getDuration()).setMultiplayerPlanet(Integer.toString(serverGameEngineControl.getPlanetConfig().getPlanetId()));
            }
            onlineInfos.add(onlineInfo);
        });
        return onlineInfos;
    }
}
