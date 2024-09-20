package com.btxtech.server;

import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 11.02.2018.
 */
public class WebsocketMessageHelper {
    private Logger logger = Logger.getLogger(WebsocketMessageHelper.class.getName());
    private List<String> messages = new ArrayList<>();

    public void clear() {
        messages.clear();
    }

    public void add(String message) {
        messages.add(message);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void assertMessageSent(int index, String expectedMessage) {
        String actualMessage = messages.get(index);
        Assert.assertEquals(expectedMessage, actualMessage);
    }

    public String assertAndExtractBody(int index, String packetString) {
        String actualMessage = messages.get(index);
        Assert.assertTrue("Message does not start with: " + packetString + "#" + ". Message: " + actualMessage, actualMessage.startsWith(packetString + "#"));
        return actualMessage.substring(packetString.length() + 1, actualMessage.length());
    }

    public <T> void assertMessageSent(int index, String packetString, Map<Integer, Integer> expected) throws IOException {
        UnlockedItemPacket actual = new ObjectMapper().readValue(assertAndExtractBody(index, packetString), UnlockedItemPacket.class);
        // ReflectionAssert.assertReflectionEquals(new UnlockedItemPacket().unlockedItemLimit(expected), actual);
    }

    public <T> void assertMessageSent(int index, String packetString, Class<T> expectedClass, T expected) throws IOException {
        Object actual = new ObjectMapper().readValue(assertAndExtractBody(index, packetString), expectedClass);
        // ReflectionAssert.assertReflectionEquals(expected, actual);
    }

    public void assertMessageSentCount(int expectedCount) {
        Assert.assertEquals("Messages sent", expectedCount, messages.size());
    }

    public void assertPacketStringSent(String packetString, int expectedCount) {
        int actualCount = 0;
        for (String s : messages) {
            if (s.startsWith(packetString + "#")) {
                actualCount++;
            }
        }
        Assert.assertEquals(expectedCount, actualCount);
    }

    public int findFirstPacketStringSentIndex(String packetString) {
        for (int i = 0; i < messages.size(); i++) {
            String s = messages.get(i);
            if (s.startsWith(packetString + "#")) {
                return i;
            }
        }
        throw new IllegalArgumentException("PacketString not sent: " + packetString);
    }

    public void printMessagesSent() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-------------------------------------------------------------------\n");
        for (String message : messages) {
            stringBuilder.append(message).append("\n");
        }
        stringBuilder.append("-------------------------------------------------------------------");
        logger.severe(stringBuilder.toString());
    }
}
