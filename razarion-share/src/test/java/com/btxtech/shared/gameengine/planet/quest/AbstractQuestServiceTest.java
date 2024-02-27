package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.basic.BaseBasicTest;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 23.09.2016.
 */
public abstract class AbstractQuestServiceTest extends BaseBasicTest {
    private final Map<Integer, QuestConfig> passedQuests = new HashMap<>();

    protected QuestListener createQuestListener() {
        return passedQuests::put;
    }

    protected void assertQuestPassed(int userId) {
        Assert.assertTrue("Quest not passed for '" + userId + "'. Quest: " + passedQuests.get(userId), passedQuests.containsKey(userId));
    }

    protected void assertQuestNotPassed(int userId) {
        Assert.assertFalse("Unexpected quest passed for '" + userId + "'. Quest: " + passedQuests.get(userId), passedQuests.containsKey(userId));
    }

    protected void assetQuestProgressCountGameLogicListener(int userId, int expectedCount, String expectedBotBasesInformation) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(userId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressCount(expectedCount, expectedBotBasesInformation, questProgressInfos.get(0));
        getTestGameLogicListener().getQuestProgresses().remove(userId);
    }

    protected void assetQuestProgressCountDownload(int userId, int expectedCount, String expectedBotBasesInformation) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(userId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressCount(expectedCount, expectedBotBasesInformation, questProgressInfo);
    }

    protected void assetQuestProgressCount(int expectedCount, String expectedBotBasesInformation, QuestProgressInfo actual) {
        Assert.assertNull(actual.getSecondsRemaining());
        Assert.assertNull(actual.getTypeCount());
        Assert.assertEquals(expectedCount, (int) actual.getCount());
        Assert.assertEquals(expectedBotBasesInformation, actual.getBotBasesInformation());
    }

    protected void assetQuestProgressTypeCountGameLogicListener(int userId, String expectedBotBasesInformation, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(userId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressTypeCount(expectedBotBasesInformation, questProgressInfos.get(0), expected);
        getTestGameLogicListener().getQuestProgresses().remove(userId);
    }

    protected void assetQuestProgressTypeCountDownload(int userId, String expectedBotBasesInformation, int... expected) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(userId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressTypeCount(expectedBotBasesInformation, questProgressInfo, expected);
    }

    protected void assertQuestProgressPositionDownload(int userId, Integer secondsRemaining, Integer secondsRemainingDelta, int... expected) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(userId);
        Assert.assertNotNull(questProgressInfo);
        assertQuestProgressPosition(questProgressInfo, secondsRemaining, secondsRemainingDelta, expected);
    }

    protected void assertQuestProgressPositionGameLogicListener(int userId, Integer secondsRemaining, Integer secondsRemainingDelta, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(userId);
        Assert.assertEquals(1, questProgressInfos.size());
        assertQuestProgressPosition(questProgressInfos.get(0), secondsRemaining, secondsRemainingDelta, expected);
        getTestGameLogicListener().getQuestProgresses().remove(userId);
    }

    protected void assertQuestProgressPositionGameLogicListenerFirst(int userId, Integer secondsRemaining, Integer secondsRemainingDelta, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(userId);
        assertQuestProgressPosition(questProgressInfos.get(0), secondsRemaining, secondsRemainingDelta, expected);
        getTestGameLogicListener().getQuestProgresses().remove(userId, questProgressInfos.get(0));
    }

    protected void assertQuestProgressPositionGameLogicListenerNone(int userId) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(userId);
        Assert.assertTrue(questProgressInfos == null || questProgressInfos.isEmpty());
    }

    protected void assertQuestProgressPosition(QuestProgressInfo actual, Integer secondsRemaining, Integer secondsRemainingDelta, int... expected) {
        if (secondsRemaining != null) {
            Assert.assertEquals(secondsRemaining, actual.getSecondsRemaining(), secondsRemainingDelta != null ? secondsRemainingDelta : 0);
        } else {
            Assert.assertNull(actual.getSecondsRemaining());
        }
        Assert.assertNull(actual.getCount());
        Assert.assertNull(actual.getBotBasesInformation());
        Assert.assertEquals(expected.length / 2, actual.getTypeCount().size());
        for (int i = 0; i < expected.length; i += 2) {
            Assert.assertEquals("BaseItemTypeId: " + expected[i], expected[i + 1], (int) actual.getTypeCount().get(expected[i]));
        }
    }

    /**
     * @param expectedBotBasesInformation bot
     * @param actual                      actual QuestProgressInfo
     * @param expected                    pair with  baseItemTypeId1, count1, baseItemTypeId1, count2 ...
     */
    protected void assetQuestProgressTypeCount(String expectedBotBasesInformation, QuestProgressInfo actual, int... expected) {
        Assert.assertEquals(expected.length / 2, actual.getTypeCount().size());
        for (int i = 0; i < expected.length; i += 2) {
            Assert.assertEquals("BaseItemTypeId: " + expected[i], expected[i + 1], (int) actual.getTypeCount().get(expected[i]));
        }
        Assert.assertNull(actual.getSecondsRemaining());
        Assert.assertNull(actual.getCount());
        Assert.assertEquals(expectedBotBasesInformation, actual.getBotBasesInformation());
    }

}