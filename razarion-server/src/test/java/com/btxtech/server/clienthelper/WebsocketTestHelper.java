package com.btxtech.server.clienthelper;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.WebsocketMessageHelper;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Assert;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 25.04.2017.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebsocketTestHelper {
    private boolean connected;
    private CountDownLatch latch = new CountDownLatch(1);
    private WebsocketMessageHelper websocketMessageHelper = new WebsocketMessageHelper();
    private TestSessionContext testSessionContext;

    public WebsocketTestHelper(String endpoint, TestSessionContext testSessionContext) {
        this.testSessionContext = testSessionContext;
        try {
            WebSocketClient client = new WebSocketClient();
            CookieStore cookieStore = new HttpCookieStore();
            // URI uri = new URI("ws://" + ClientArquillianBaseTest.HOST_PORT + endpoint);
            URI uri = new URI("ws://" + ClientArquillianBaseTest.HOST_PORT + "/test" + endpoint);
            cookieStore.add(uri, new HttpCookie(testSessionContext.getSessionCookie().getName(), testSessionContext.getSessionCookie().getValue()));
            client.setCookieStore(cookieStore);
            client.start();
            client.connect(this, uri, new ClientUpgradeRequest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TestSessionContext getTestSessionContext() {
        return testSessionContext;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        connected = false;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        connected = true;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        websocketMessageHelper.add(message);
        latch.countDown();
    }

    public void waitForDelivery() {
        if (!connected) {
            throw new IllegalStateException("WebsocketTestHelper is not connected");
        }
        if (!websocketMessageHelper.isEmpty()) {
            return;
        }
        try {
            if(!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Nothing delivered");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void assertNoDelivery(long milliseconds) {
        if (!connected) {
            throw new IllegalStateException("WebsocketTestHelper is not connected");
        }
        if (!websocketMessageHelper.isEmpty()) {
            websocketMessageHelper.printMessagesSent();
            Assert.fail("Unexpected delivery");
        }
        try {
            if(latch.await(milliseconds, TimeUnit.MILLISECONDS)) {
                websocketMessageHelper.printMessagesSent();
                Assert.fail("Unexpected delivery");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WebsocketMessageHelper getWebsocketMessageHelper() {
        if (!connected) {
            throw new IllegalStateException("WebsocketTestHelper is not connected");
        }
        return websocketMessageHelper;
    }
}
