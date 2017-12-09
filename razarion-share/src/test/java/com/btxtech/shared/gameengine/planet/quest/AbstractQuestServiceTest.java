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
        return (humanPlayerId, questConfig) -> {
            passedQuests.put(humanPlayerId, questConfig);
        };
    }

    protected void assertQuestPassed(HumanPlayerId humanPlayerId) {
        Assert.assertTrue("Quest not passed for '" + humanPlayerId + "'. Quest: " + passedQuests.get(humanPlayerId), passedQuests.containsKey(humanPlayerId));
    }

    protected void assertQuestNotPassed(HumanPlayerId humanPlayerId) {
        Assert.assertFalse("Unexpected quest passed for '" + humanPlayerId + "'. Quest: " + passedQuests.get(humanPlayerId), passedQuests.containsKey(humanPlayerId));
    }

    protected void assetQuestProgressCountGameLogicListener(HumanPlayerId humanPlayerId, int expectedCount) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(humanPlayerId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressCount(expectedCount, questProgressInfos.get(0));
        getTestGameLogicListener().getQuestProgresses().remove(humanPlayerId);
    }

    protected void assetQuestProgressCountDownload(HumanPlayerId humanPlayerId, int expectedCount) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(humanPlayerId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressCount(expectedCount, questProgressInfo);
    }

    protected void assetQuestProgressCount(int expectedCount, QuestProgressInfo actual) {
        Assert.assertNull(actual.getTime());
        Assert.assertNull(actual.getTypeCount());
        Assert.assertEquals(expectedCount, (int) actual.getCount());
    }

    protected void assetQuestProgressTypeCountGameLogicListener(HumanPlayerId humanPlayerId, int... expected) {
        List<QuestProgressInfo> questProgressInfos = getTestGameLogicListener().getQuestProgresses().get(humanPlayerId);
        Assert.assertEquals(1, questProgressInfos.size());
        assetQuestProgressTypeCount(questProgressInfos.get(0), expected);
        getTestGameLogicListener().getQuestProgresses().remove(humanPlayerId);
    }

    protected void assetQuestProgressTypeCountDownload(HumanPlayerId humanPlayerId, int... expected) {
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(humanPlayerId);
        Assert.assertNotNull(questProgressInfo);
        assetQuestProgressTypeCount(questProgressInfo, expected);
    }

    protected void assetQuestProgressTypeCount(QuestProgressInfo actual, int... expected) {
        Assert.assertEquals(expected.length / 2, actual.getTypeCount().size());
        for (int i = 0; i < expected.length; i += 2) {
            Assert.assertEquals(expected[i + 1], (int) actual.getTypeCount().get(expected[i]));
        }
        Assert.assertNull(actual.getTime());
        Assert.assertNull(actual.getCount());

    }

}