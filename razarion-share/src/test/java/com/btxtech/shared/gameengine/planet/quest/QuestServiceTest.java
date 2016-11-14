package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.UserContext;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.09.2016.
 */
@RunWith(EasyMockRunner.class)
public class QuestServiceTest {
    @Mock(type = MockType.STRICT)
    private Consumer<UserContext> conditionPassedListenerMock;

    @Test
    public void test1() {
        // TODO
//        UserContext userContext = new UserContext().setName("QuestServiceTest Base");
//
//        conditionPassedListenerMock.accept(userContext);
//        EasyMock.replay(conditionPassedListenerMock);
//        QuestService conditionService = new QuestService();
//
//        SimpleTestEnvironment simpleTestEnvironment = new SimpleTestEnvironment();
//        simpleTestEnvironment.injectItemTypeService(conditionService);
//
//        ConditionConfig conditionConfig = new ConditionConfig();
//        conditionConfig.setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED);
//        Map<Integer, Integer> baseItemTypeCount = new HashMap<>();
//        baseItemTypeCount.put(SimpleTestEnvironment.SIMPLE_MOVABLE_ITEM_TYPE.getId(), 1);
//        conditionConfig.setComparisonConfig(new ComparisonConfig().setTypeCount(baseItemTypeCount));
//
//        conditionService.activateCondition(userContext, conditionConfig, conditionPassedListenerMock);
//
//        PlayerBase playerBase = simpleTestEnvironment.createHumanPlayerBase(userContext);
//        conditionService.onSyncItemBuilt(simpleTestEnvironment.createSimpleSyncBaseItem(playerBase));
//
//        EasyMock.verify(conditionPassedListenerMock);
    }

}