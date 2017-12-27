package com.btxtech.server;

import com.btxtech.server.gameengine.ClientGameConnection;
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
public class TestClientGameConnection extends ClientGameConnection {
    private PlayerSession playerSession;
    private List<String> messagesSent = new ArrayList<>();

    public TestClientGameConnection(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    @Override
    public void sendToClient(String text) {
        messagesSent.add(text);
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

    public void assertPacketStringSent(String packetString, int expectedCount) {
        int actualCount = 0;
        for (String s : messagesSent) {
            if (s.startsWith(packetString + "#")) {
                actualCount++;
            }
        }
        Assert.assertEquals(expectedCount, actualCount);
    }

    public int findFirstPacketStringSentIndex(String packetString) {
        for (int i = 0; i < messagesSent.size(); i++) {
            String s = messagesSent.get(i);
            if (s.startsWith(packetString + "#")) {
                return i;
            }
        }
        throw new IllegalArgumentException("PacketString not sent: " + packetString);
    }

    public String assertAndExtractBody(int index, String packetString) {
        String actualMessage = messagesSent.get(index);
        Assert.assertTrue("Message does not start with: " + packetString + "#" + ". Message: " + actualMessage, actualMessage.startsWith(packetString + "#"));
        return actualMessage.substring(packetString.length() + 1, actualMessage.length());
    }

    public void assertMessageSent(int index, String packetString, Map<Integer, Integer> expected) throws IOException {
        UnlockedItemPacket actual = new ObjectMapper().readValue(assertAndExtractBody(index, packetString), UnlockedItemPacket.class);
        ReflectionAssert.assertReflectionEquals(new UnlockedItemPacket().setUnlockedItemLimit(expected), actual);
    }

    public <T> void assertMessageSent(int index, String packetString, Class<T> expectedClass, T expected) throws IOException {
        Object actual = new ObjectMapper().readValue(assertAndExtractBody(index, packetString), expectedClass);
        ReflectionAssert.assertReflectionEquals(expected, actual);
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
