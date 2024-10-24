package com.btxtech.server;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.user.PlayerSession;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 27.08.2017.
 */
@Singleton
public class ClientSystemConnectionServiceTestHelper {

    private ClientSystemConnectionService clientSystemConnectionService;

    @Inject
    public ClientSystemConnectionServiceTestHelper(ClientSystemConnectionService clientSystemConnectionService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
    }

    public TestClientSystemConnection connectClient(PlayerSession playerSession) {
        TestClientSystemConnection testClientSystemConnection = new TestClientSystemConnection(playerSession);
        clientSystemConnectionService.onOpen(testClientSystemConnection);
        return testClientSystemConnection;
    }

}
