package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.InputService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InGameQuestVisualizationServiceBaseItemTypeTest extends DaggerUiBaseIntegrationTest {
    private static final int OWN_BASE_ITEM_ID = 1;
    private static final int BOT_BASE_ITEM_ID = 3;

    @Test
    public void activateBaseItemInsideScrollOutside() {
        ColdGameUiContext coldGameUiContext = setup();

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[3];
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 1;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 140;
        nativeSyncBaseItemTickInfo.y = 185;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[0] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 2;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.CONSUMER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 160;
        nativeSyncBaseItemTickInfo.y = 175;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[1] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 3;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.HARVESTER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 180;
        nativeSyncBaseItemTickInfo.y = 175;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[2] = nativeSyncBaseItemTickInfo;

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        createBase(coldGameUiContext.getUserContext().getUserId(), OWN_BASE_ITEM_ID);
        Map<Integer, Integer> questTypeCount = new HashMap<>();
        questTypeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        questTypeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 2);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().typeCount(questTypeCount))));

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(3, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        Assert.assertNotNull(findBaseItem(1).getMarkerConfig());
        Assert.assertNotNull(findBaseItem(2).getMarkerConfig());
        Assert.assertNull(findBaseItem(3).getMarkerConfig());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(264, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());

        Map<Integer, Integer> progressTypeCount = new HashMap<>();
        progressTypeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        progressTypeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 1);
        getWeldBean(InGameQuestVisualizationService.class).onQuestProgress(new QuestProgressInfo().setTypeCount(progressTypeCount));
        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(275.4, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
    }

    @Test
    public void activateBaseItemOutsideScrollInside() {
        ColdGameUiContext coldGameUiContext = setup();

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[3];
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 1;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 160;
        nativeSyncBaseItemTickInfo.y = 250;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[0] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 2;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.CONSUMER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 140;
        nativeSyncBaseItemTickInfo.y = 250;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[1] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 3;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.HARVESTER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 120;
        nativeSyncBaseItemTickInfo.y = 250;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[2] = nativeSyncBaseItemTickInfo;

        createBase(coldGameUiContext.getUserContext().getUserId(), OWN_BASE_ITEM_ID);
        Map<Integer, Integer> questTypeCount = new HashMap<>();
        questTypeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        questTypeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 2);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().typeCount(questTypeCount))));

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(85.6, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);
        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(3, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        Assert.assertNotNull(findBaseItem(1).getMarkerConfig());
        Assert.assertNotNull(findBaseItem(2).getMarkerConfig());
        Assert.assertNull(findBaseItem(3).getMarkerConfig());

        Map<Integer, Integer> progressTypeCount = new HashMap<>();
        progressTypeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        progressTypeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 1);
        getWeldBean(InGameQuestVisualizationService.class).onQuestProgress(new QuestProgressInfo().setTypeCount(progressTypeCount));
        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(3, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        Assert.assertNull(findBaseItem(1).getMarkerConfig());
        Assert.assertNotNull(findBaseItem(2).getMarkerConfig());
        Assert.assertNull(findBaseItem(3).getMarkerConfig());
    }

    private ColdGameUiContext setup() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId(1)
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();

        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 40,
                240, 40,
                300, 200,
                0, 200);

        setupI18nConstants();
        setupCockpit();

        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Bot test base")
                .baseId(BOT_BASE_ITEM_ID)
                .userId(11)
                .character(Character.BOT));

        return coldGameUiContext;
    }
}