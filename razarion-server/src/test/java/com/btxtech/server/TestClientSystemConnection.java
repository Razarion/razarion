package com.btxtech.server;

import com.btxtech.server.connection.ClientSystemConnection;
import com.btxtech.server.user.PlayerSession;
import org.junit.Assert;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 27.08.2017.
 */
public class TestClientSystemConnection extends ClientSystemConnection {
    private PlayerSession playerSession;
    private List<String> messagesSent = new ArrayList<>();

    public TestClientSystemConnection(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    @Override
    public void sendToClient(String text) {
        messagesSent.add(text);
    }

    @Override
    public PlayerSession getSession() {
        return playerSession;
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

    public void assertMessageSent(int index, String expectedMessage) {
        String actualMessage = messagesSent.get(index);
        Assert.assertEquals(expectedMessage, actualMessage);
    }

    public void assertMessageSentCount(int expectedCount) {
        Assert.assertEquals("Messages sent", expectedCount, messagesSent.size());
    }

    public void printMessagesSent() {
        System.out.println("-------------------------------------------------------------------");
        for (String message : messagesSent) {
            System.out.println(message);
        }
        System.out.println("-------------------------------------------------------------------");
    }
}