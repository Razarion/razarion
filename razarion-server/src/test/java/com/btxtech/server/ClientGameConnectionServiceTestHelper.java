package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnectionService;
import com.btxtech.server.user.PlayerSession;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 27.08.2017.
 */
@Singleton
public class ClientGameConnectionServiceTestHelper {
    @Inject
    private ClientGameConnectionService clientGameConnectionService;

    public TestClientGameConnection connectClient(PlayerSession playerSession) {
        TestClientGameConnection testClientGameConnection = new TestClientGameConnection(playerSession);
        clientGameConnectionService.onOpen(testClientGameConnection, playerSession.getUserContext().getUserId());
        return testClientGameConnection;
    }

}
