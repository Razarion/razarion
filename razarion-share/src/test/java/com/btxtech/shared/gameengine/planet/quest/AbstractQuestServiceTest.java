package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.HumanPlayerId;
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
    private Map<HumanPlayerId, QuestConfig> passedQuests = new HashMap<>();

    protected QuestListener createQuestListener() {
        return passedQuests::put;
    }

    protected void assertQuestPassed(HumanPlayerId humanPlayerId) {
        Assert.assertTrue("Quest not passed for '" + humanPlayerId + "'. Quest: " + passedQuests.get(humanPlayerId), passedQuests.containsKey(humanPlayerId));
    }

    protected void assertQuestNotPassed(HumanPlayerId humanPlayerId) {
        Assert.assertFalse("Unexpected quest passed for '" + humanPlayerId + "'. Quest: " + passedQuests.get(humanPlayerId), passedQuests.containsKey(humanPlayerId));
    }

    protected void assetQuestProgressCountGameLogicListener(HumanPlayerId humanPlayerId, int expectedCount, String expectedBotBasesInformation) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(humanPlayerId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressCount(expectedCount, expectedBotBasesInformation, questProgressInfos.get(0));
        getTestGameLogicListener().getQuestProgresses().remove(humanPlayerId);
    }

    protected void assetQuestProgressCountDownload(HumanPlayerId humanPlayerId, int expectedCount, String expectedBotBasesInformation) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(humanPlayerId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressCount(expectedCount, expectedBotBasesInformation, questProgressInfo);
    }

    protected void assetQuestProgressCount(int expectedCount, String expectedBotBasesInformation, QuestProgressInfo actual) {
        Assert.assertNull(actual.getTime());
        Assert.assertNull(actual.getTypeCount());
        Assert.assertEquals(expectedCount, (int) actual.getCount());
        Assert.assertEquals(expectedBotBasesInformation, actual.getBotBasesInformation());
    }

    protected void assetQuestProgressTypeCountGameLogicListener(HumanPlayerId humanPlayerId, String expectedBotBasesInformation, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(humanPlayerId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressTypeCount(expectedBotBasesInformation, questProgressInfos.get(0), expected);
        getTestGameLogicListener().getQuestProgresses().remove(humanPlayerId);
    }

    protected void assetQuestProgressTypeCountDownload(HumanPlayerId humanPlayerId, String expectedBotBasesInformation, int... expected) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(humanPlayerId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressTypeCount(expectedBotBasesInformation, questProgressInfo, expected);
    }

    protected void assertQuestProgressPositionDownload(HumanPlayerId humanPlayerId, int... expected) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(humanPlayerId);
        Assert.assertNotNull(questProgressInfo);
        assertQuestProgressPosition(questProgressInfo, expected);
    }

    protected void assertQuestProgressPositionGameLogicListener(HumanPlayerId humanPlayerId, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(humanPlayerId);
        Assert.assertEquals(1, questProgressInfos.size());
        assertQuestProgressPosition(questProgressInfos.get(0), expected);
        getTestGameLogicListener().getQuestProgresses().remove(humanPlayerId);
    }

    protected void assertQuestProgressPosition(QuestProgressInfo actual, int... expected) {
        Assert.assertNull(actual.getTime());
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
        Assert.assertNull(actual.getTime());
        Assert.assertNull(actual.getCount());
        Assert.assertEquals(expectedBotBasesInformation, actual.getBotBasesInformation());
    }

}