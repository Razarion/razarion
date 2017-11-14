package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 23.09.2016.
 */
public abstract class AbstractQuestServiceTest extends WeldMasterBaseTest {
    private Map<HumanPlayerId, QuestConfig> passedQuests = new HashMap<>();

    protected void setup() {
        setupMasterEnvironment();
    }


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

}