package com.btxtech.server.connection;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.server.clienthelper.WebsocketTestHelper;
import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.rest.BackendProvider;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.rest.FrontendController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 11.02.2018.
 */
@Ignore
public class SystemConnectionTest extends ClientArquillianBaseTest {

    @Before
    public void before() {
        startFakeMailServer();
        setupPlanets();
    }

    @After
    public void after() {
        stopFakeMailServer();
        cleanUsers();
        cleanPlanets();
    }

    @Test
    public void testConnection() {
        // TODO  Fails due to websocket and httpSessionId problem. Fix sending httpsessionid over WebSocket
        List<WebsocketTestHelper> websocketTestHelpers = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            websocketTestHelpers.add(createConnection(i));
        }

        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage("en-Us");
        BackendProvider backendProvider = setupClient(BackendProvider.class, testSessionContext);
        List<OnlineInfo> onlineInfos = backendProvider.loadAllOnlines();
        Assert.assertEquals(40, onlineInfos.size());
        Set<String> sessions = onlineInfos.stream().map(OnlineInfo::getSessionId).collect(Collectors.toSet());

//        // -------------------------------------------------------------------
//        Collection<String> existingSessions = new ArrayList<>();
//        for (OnlineInfo onlineInfo : onlineInfos) {
//            if(existingSessions.contains(onlineInfo.getSessionId())) {
//                System.out.println("Doubled sessionId: " + onlineInfo.getSessionId());
//            } else {
//                existingSessions.add(onlineInfo.getSessionId());
//            }
//        }
//        // -------------------------------------------------------------------


        Assert.assertEquals(40, sessions.size());

        for (WebsocketTestHelper websocketTestHelper : websocketTestHelpers) {
            if (!sessions.remove(websocketTestHelper.getTestSessionContext().getSessionId())) {
                Assert.fail("Connection not found: " + websocketTestHelper.getTestSessionContext().getSessionId());
            }
        }
        Assert.assertTrue("Unexpected session: " + sessions, sessions.isEmpty());
    }

    private WebsocketTestHelper createConnection(int i) {
        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage("en-Us");
        FrontendController frontendController = setupClient(FrontendController.class, testSessionContext);
        Assert.assertFalse(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertEquals(RegisterResult.OK, frontendController.createUnverifiedUser("xxxx" + i + "@yyyy.com", "0123456789", false));
        return new WebsocketTestHelper(CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT, testSessionContext);
    }
}
