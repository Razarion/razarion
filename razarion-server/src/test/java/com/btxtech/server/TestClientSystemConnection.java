package com.btxtech.server;

import com.btxtech.server.connection.ClientSystemConnection;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 27.08.2017.
 */
public class TestClientSystemConnection extends ClientSystemConnection {
    private PlayerSession playerSession;
    private WebsocketMessageHelper websocketMessageHelper = new WebsocketMessageHelper();

    public TestClientSystemConnection(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void clear() {
        websocketMessageHelper.clear();
    }

    @Override
    public void sendToClient(String text) {
        websocketMessageHelper.add(text);
    }

    @Override
    public PlayerSession getSession() {
        return playerSession;
    }

    public WebsocketMessageHelper getWebsocketMessageHelper() {
        return websocketMessageHelper;
    }

    @Override
    public void onMessage(Session session, String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void open(Session session, EndpointConfig config) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void error(Session session, Throwable error) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(Session session, CloseReason reason) {
        throw new UnsupportedOperationException();
    }
}
