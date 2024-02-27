package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.basic.HumanBaseContext;
import org.junit.Test;

import java.util.Arrays;

public class UnlockQuestTest extends AbstractQuestServiceTest {
    @Test
    public void test1() {
        setup();
        HumanBaseContext humanBaseContext = createHumanBaseBFA();

        QuestConfig questConfig = new QuestConfig()
                .id(19001)
                .conditionConfig(new ConditionConfig()
                        .conditionTrigger(ConditionTrigger.UNLOCKED)
                        .comparisonConfig(new ComparisonConfig().count(1)));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getUserId(), questConfig);

        assertQuestNotPassed(humanBaseContext.getUserContext().getUserId());

        getQuestService().onUnlock(humanBaseContext.getUserContext().getUserId());

        assertQuestPassed(humanBaseContext.getUserContext().getUserId());

    }
}
